#!/usr/bin/env bash
# kernel-notification-service local runner — Linux, macOS, Windows Git Bash/MSYS.
# Windows cmd: use run-local.bat (standalone; no .ps1).
#
# Uses src/main/resources/application-local.properties (profile local).
# SMS/email are proxied (not sent). No config-server required.
set -euo pipefail

MODULE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KERNEL_DIR="$(cd "${MODULE_DIR}/.." && pwd)"
LOCAL_DIR="${MODULE_DIR}/.local"
LOG_DIR="${LOCAL_DIR}/logs"
PID_DIR="${LOCAL_DIR}/pids"
PORT="${NOTIFIER_PORT:-8083}"
PID_FILE="${PID_DIR}/notifier.pid"
LOG_FILE="${LOG_DIR}/notifier.log"
MODULE="kernel-notification-service"
UNAME_S="$(uname -s 2>/dev/null || echo unknown)"
CONTEXT="/v1/notifier"

MVN_SKIP=(
  "-DskipTests"
  "-Dgpg.skip=true"
  "-Dmaven.javadoc.skip=true"
)

usage() {
  cat <<'EOF'
Local kernel-notification-service (local profile, :8083 /v1/notifier)

  Linux / macOS / Git Bash:
    ./run-local.sh init | start | smoke | stop | test | all

  Windows cmd:
    run-local.bat init | start | smoke | stop | test | all

Config: src/main/resources/application-local.properties
SMS/email: proxied (mosip.kernel.sms.proxy-sms / mosip.kernel.mail.proxy-mail)

  all = init + test + start + smoke
EOF
  exit "${1:-0}"
}

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "error: '$1' is required on PATH" >&2
    exit 1
  }
}

ensure_dirs() {
  mkdir -p "$LOG_DIR" "$PID_DIR"
}

check_prereqs() {
  need_cmd java
  need_cmd mvn
  echo "os: ${UNAME_S}"
  local ver
  ver="$(java -version 2>&1 | head -n 1 || true)"
  echo "java: $ver"
  if ! echo "$ver" | grep -E '"21[\. "]' >/dev/null 2>&1; then
    echo "warn: JDK 21 is required. Continuing anyway." >&2
  fi
}

find_boot_jar() {
  local target="${MODULE_DIR}/target"
  local jar
  if [[ ! -d "$target" ]]; then
    echo "error: not packaged. Run: $0 init" >&2
    exit 1
  fi
  for jar in "$target"/${MODULE}-*.jar; do
    [[ -f "$jar" ]] || continue
    case "$jar" in
      *.original|*sources*|*javadoc*) continue ;;
    esac
    echo "$jar"
    return 0
  done
  echo "error: no boot jar in ${target}. Run: $0 init" >&2
  exit 1
}

is_running() {
  [[ -f "$PID_FILE" ]] || return 1
  kill -0 "$(cat "$PID_FILE")" 2>/dev/null
}

cmd_init() {
  check_prereqs
  # Running java -jar holds the boot jar; Maven clean cannot delete it.
  cmd_stop
  echo "==> packaging ${MODULE} (skip tests)"
  (cd "$KERNEL_DIR" && mvn -pl "$MODULE" -am clean package "${MVN_SKIP[@]}")
  echo "init complete"
}

cmd_test() {
  check_prereqs
  echo "==> maven tests"
  (cd "$KERNEL_DIR" && mvn -pl "$MODULE" -am test "-Dgpg.skip=true" "-Dmaven.javadoc.skip=true")
}

cmd_start() {
  echo
  echo "==> run-local start"
  ensure_dirs
  check_prereqs
  if is_running; then
    echo "notifier already running (pid $(cat "$PID_FILE")) on port ${PORT}"
    echo "  health   http://127.0.0.1:${PORT}${CONTEXT}/actuator/health"
    echo "  swagger  http://127.0.0.1:${PORT}${CONTEXT}/swagger-ui/index.html"
    return 0
  fi
  rm -f "$LOG_FILE"
  local jar
  jar="$(find_boot_jar)"
  echo "==> starting notifier from $(basename "$jar")"
  echo "    profile=local"
  echo "    port=${PORT}"
  echo "    context=${CONTEXT}"
  echo "    properties=src/main/resources/application-local.properties"
  local java_args=(
    "-Dspring.profiles.active=local"
    "-Dserver.port=${PORT}"
    "-Dspring.cloud.config.enabled=false"
    "-Dspring.sleuth.enabled=false"
    "-Dmanagement.health.mail.enabled=false"
  )
  java_args+=(-jar "$jar")
  nohup java "${java_args[@]}" >"$LOG_FILE" 2>&1 &
  echo $! >"$PID_FILE"
  echo "pid $(cat "$PID_FILE")  log ${LOG_FILE}"
  echo "==> waiting for Spring Boot startup on port ${PORT} (up to 90s) ..."
  wait_ready 90
  echo
  echo "notifier ready"
  echo "  port     ${PORT}"
  echo "  health   http://127.0.0.1:${PORT}${CONTEXT}/actuator/health"
  echo "  swagger  http://127.0.0.1:${PORT}${CONTEXT}/swagger-ui/index.html"
  echo "  email    POST http://127.0.0.1:${PORT}${CONTEXT}/email/send"
  echo "  sms      POST http://127.0.0.1:${PORT}${CONTEXT}/sms/send"
}

wait_ready() {
  local timeout="${1:-90}"
  local elapsed=0
  local code
  while [[ "$elapsed" -lt "$timeout" ]]; do
    if ! is_running; then
      echo "error: process exited during startup. See ${LOG_FILE}" >&2
      tail -n 80 "$LOG_FILE" >&2 || true
      return 1
    fi
    if grep -q "Started NotificationBootApplication" "$LOG_FILE" 2>/dev/null; then
      code="$(http_ok "http://127.0.0.1:${PORT}${CONTEXT}/actuator/health")"
      if [[ "$code" == "200" ]]; then
        return 0
      fi
      code="$(http_ok "http://127.0.0.1:${PORT}/actuator/health")"
      if [[ "$code" == "200" ]]; then
        return 0
      fi
    fi
    code="$(http_ok "http://127.0.0.1:${PORT}${CONTEXT}/actuator/health")"
    if [[ "$code" == "200" ]]; then
      return 0
    fi
    if grep -q "APPLICATION FAILED TO START\|Port ${PORT} was already in use" "$LOG_FILE" 2>/dev/null; then
      echo "error: startup failed. See ${LOG_FILE}" >&2
      tail -n 80 "$LOG_FILE" >&2 || true
      return 1
    fi
    echo "    ... still starting (${elapsed}s / ${timeout}s)"
    sleep 2
    elapsed=$((elapsed + 2))
  done
  echo "error: Spring Boot did not become ready within ${timeout}s on port ${PORT}" >&2
  tail -n 80 "$LOG_FILE" >&2 || true
  return 1
}

http_ok() {
  local url="$1"
  local code
  if command -v curl >/dev/null 2>&1; then
    code="$(curl -sS -o /dev/null -w "%{http_code}" --connect-timeout 2 --max-time 3 "$url" 2>/dev/null || true)"
    [[ -n "$code" ]] || code="000"
    printf '%s' "$code"
    return 0
  fi
  if command -v python3 >/dev/null 2>&1; then
    python3 -c 'import sys,urllib.request
try:
    urllib.request.urlopen(sys.argv[1], timeout=3)
    print("200")
except Exception:
    print("000")' "$url"
    return 0
  fi
  if command -v python >/dev/null 2>&1; then
    python -c 'import sys,urllib.request
try:
    urllib.request.urlopen(sys.argv[1], timeout=3)
    print("200")
except Exception:
    print("000")' "$url"
    return 0
  fi
  echo "error: install curl or python3 for smoke checks" >&2
  printf '%s' "000"
}

cmd_stop() {
  ensure_dirs
  if ! is_running; then
    rm -f "$PID_FILE"
    echo "notifier is not running"
    return 0
  fi
  local pid
  pid="$(cat "$PID_FILE")"
  echo "==> stopping notifier (${pid}) on port ${PORT}"
  kill "$pid" 2>/dev/null || true
  local i
  for i in 1 2 3 4 5 6 7 8 9 10; do
    kill -0 "$pid" 2>/dev/null || break
    sleep 1
  done
  if kill -0 "$pid" 2>/dev/null; then
    kill -9 "$pid" 2>/dev/null || true
  fi
  rm -f "$PID_FILE"
  echo "stopped."
}

cmd_smoke() {
  ensure_dirs
  local timeout="${1:-60}"
  local elapsed=0
  echo "==> waiting for notifier (up to ${timeout}s)"
  while [[ "$elapsed" -lt "$timeout" ]]; do
    if [[ -f "$PID_FILE" ]] && ! is_running; then
      echo "error: process exited. See ${LOG_FILE}" >&2
      tail -n 40 "$LOG_FILE" >&2 || true
      return 1
    fi
    local url code health_url=""
    for url in \
      "http://127.0.0.1:${PORT}${CONTEXT}/actuator/health" \
      "http://127.0.0.1:${PORT}/actuator/health"
    do
      code="$(http_ok "$url")"
      if [[ "$code" == "200" ]]; then
        health_url="$url"
        break
      fi
    done
    if [[ -n "$health_url" ]]; then
      echo "healthy  ${health_url}"
      code="$(http_ok "http://127.0.0.1:${PORT}${CONTEXT}/v3/api-docs")"
      echo "openapi  http://127.0.0.1:${PORT}${CONTEXT}/v3/api-docs  HTTP ${code}"
      if [[ "$code" != "200" ]]; then
        echo "error: openapi endpoint not healthy" >&2
        return 1
      fi
      echo "smoke ok"
      return 0
    fi
    sleep 2
    elapsed=$((elapsed + 2))
  done
  echo "error: not healthy. See ${LOG_FILE}" >&2
  tail -n 80 "$LOG_FILE" >&2 || true
  return 1
}

cmd_all() {
  echo "==> all: init + test + start + smoke"
  cmd_init
  cmd_test
  cmd_start
  cmd_smoke
}

main() {
  local cmd="${1:-}"
  shift || true
  case "$cmd" in
    -h|--help|help) usage 0 ;;
    init) cmd_init ;;
    test) cmd_test ;;
    start) cmd_start ;;
    stop) cmd_stop ;;
    smoke) cmd_smoke "${1:-60}" ;;
    all) cmd_all ;;
    "") usage 1 ;;
    *) echo "error: unknown command '$cmd'" >&2; usage 1 ;;
  esac
}

main "$@"
