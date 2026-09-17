#!/usr/bin/env bash
# kernel-idgenerator-service local runner — Linux, macOS, Windows Git Bash/MSYS.
# Windows cmd: use run-local.bat (standalone; no .ps1).
#
# Uses src/main/resources/application-local.properties (profile local).
# In-memory H2. No config-server or Postgres required.
set -euo pipefail

MODULE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KERNEL_DIR="$(cd "${MODULE_DIR}/.." && pwd)"
LOCAL_DIR="${MODULE_DIR}/.local"
LOG_DIR="${LOCAL_DIR}/logs"
PID_DIR="${LOCAL_DIR}/pids"
PORT="${IDGENERATOR_PORT:-8080}"
PID_FILE="${PID_DIR}/idgenerator.pid"
LOG_FILE="${LOG_DIR}/idgenerator.log"
MODULE="kernel-idgenerator-service"
UNAME_S="$(uname -s 2>/dev/null || echo unknown)"
CONTEXT="/v1/idgenerator"
RID_CONTEXT="/v1/ridgenerator"

MVN_SKIP=(
  "-DskipTests"
  "-Dmaven.test.skip=true"
  "-Dgpg.skip=true"
  "-Dmaven.javadoc.skip=true"
)

usage() {
  cat <<'EOF'
Local kernel-idgenerator-service (local profile, :8080 /v1/idgenerator + /v1/ridgenerator)

  Linux / macOS / Git Bash:
    ./run-local.sh init | start | smoke | stop | test | all

  Windows cmd:
    run-local.bat init | start | smoke | stop | test | all

Config: src/main/resources/application-local.properties
DB: in-memory H2 (no Postgres required)

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

print_urls() {
  echo "  health   http://127.0.0.1:${PORT}${CONTEXT}/actuator/health"
  echo "  uin      GET http://127.0.0.1:${PORT}${CONTEXT}/uin"
  echo "  vid      GET http://127.0.0.1:${PORT}${CONTEXT}/vid"
  echo "  rid      GET http://127.0.0.1:${PORT}${RID_CONTEXT}/generate/rid/{centerid}/{machineid}"
}

cmd_start() {
  echo
  echo "==> run-local start"
  ensure_dirs
  check_prereqs
  if is_running; then
    echo "idgenerator already running (pid $(cat "$PID_FILE")) on port ${PORT}"
    print_urls
    return 0
  fi
  rm -f "$LOG_FILE"
  local jar
  jar="$(find_boot_jar)"
  echo "==> starting idgenerator from $(basename "$jar")"
  echo "    profile=local"
  echo "    port=${PORT}"
  echo "    context=${CONTEXT}  rid=${RID_CONTEXT}"
  echo "    properties=src/main/resources/application-local.properties"
  echo "    db=H2 in-memory"
  local java_args=(
    "-Dspring.profiles.active=local"
    "-Dserver.port=${PORT}"
    "-Dspring.cloud.config.enabled=false"
    "-Dspring.sleuth.enabled=false"
  )
  java_args+=(-jar "$jar")
  nohup java "${java_args[@]}" >"$LOG_FILE" 2>&1 &
  echo $! >"$PID_FILE"
  echo "pid $(cat "$PID_FILE")  log ${LOG_FILE}"
  echo "==> waiting for Vert.x HTTP on port ${PORT} (up to 180s; UIN/VID pool first) ..."
  wait_ready 180
  echo
  echo "idgenerator ready"
  echo "  port     ${PORT}"
  print_urls
}

wait_ready() {
  local timeout="${1:-180}"
  local elapsed=0
  local code
  while [[ "$elapsed" -lt "$timeout" ]]; do
    if ! is_running; then
      echo "error: process exited during startup. See ${LOG_FILE}" >&2
      tail -n 80 "$LOG_FILE" >&2 || true
      return 1
    fi
    if grep -q "Deployed verticle HttpServerVerticle\|Starting vidgenerator service" "$LOG_FILE" 2>/dev/null; then
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
    if grep -q "APPLICATION FAILED TO START\|Port ${PORT} was already in use\|Failed to deploy verticle HttpServerVerticle\|vid fetcher verticle deployment failed" "$LOG_FILE" 2>/dev/null; then
      echo "error: startup failed. See ${LOG_FILE}" >&2
      tail -n 80 "$LOG_FILE" >&2 || true
      return 1
    fi
    echo "    ... still starting (${elapsed}s / ${timeout}s)"
    sleep 2
    elapsed=$((elapsed + 2))
  done
  echo "error: Vert.x HTTP did not become ready within ${timeout}s on port ${PORT}" >&2
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
    echo "idgenerator is not running"
    return 0
  fi
  local pid
  pid="$(cat "$PID_FILE")"
  echo "==> stopping idgenerator (${pid}) on port ${PORT}"
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
  echo "==> waiting for idgenerator (up to ${timeout}s)"
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
      code="$(http_ok "http://127.0.0.1:${PORT}${CONTEXT}/metrics")"
      echo "metrics  http://127.0.0.1:${PORT}${CONTEXT}/metrics  HTTP ${code}"
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
