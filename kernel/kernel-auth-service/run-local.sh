#!/usr/bin/env bash
# kernel-auth-service local runner — Linux, macOS, Windows Git Bash/MSYS.
# Windows cmd: use run-local.bat (standalone; no .ps1).
#
# Uses src/main/resources/application-local.properties (profile local).
# Optional: SPRING_CLOUD_CONFIG_URI SPRING_CLOUD_CONFIG_LABEL PORT JDK_JAVA_OPTIONS
set -euo pipefail

MODULE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KERNEL_DIR="$(cd "${MODULE_DIR}/.." && pwd)"
LOCAL_DIR="${MODULE_DIR}/.local"
LOG_DIR="${LOCAL_DIR}/logs"
PID_DIR="${LOCAL_DIR}/pids"
PORT="${PORT:-8091}"
PID_FILE="${PID_DIR}/auth-service.pid"
LOG_FILE="${LOG_DIR}/auth-service.log"
MODULE="kernel-auth-service"
PROFILE="${SPRING_PROFILES_ACTIVE:-local}"
CONFIG_URI="${SPRING_CLOUD_CONFIG_URI:-}"
CONFIG_LABEL="${SPRING_CLOUD_CONFIG_LABEL:-}"
IMAGE="${IMAGE:-kernel-auth-service}"
CONTEXT="/v1/authmanager"
BASE="http://127.0.0.1:${PORT}${CONTEXT}"
UNAME_S="$(uname -s 2>/dev/null || echo unknown)"

MVN_SKIP=(
  "-DskipTests"
  "-Dgpg.skip=true"
  "-Dmaven.javadoc.skip=true"
)

usage() {
  cat <<'EOF'
Local kernel-auth-service (local profile, :8091 /v1/authmanager)

  Linux / macOS / Git Bash:
    ./run-local.sh init | start | smoke | stop | test | all | docker

  Windows cmd:
    run-local.bat init | start | smoke | stop | test | all | docker

Config: src/main/resources/application-local.properties
Optional: SPRING_CLOUD_CONFIG_URI SPRING_CLOUD_CONFIG_LABEL PORT JDK_JAVA_OPTIONS IMAGE

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

print_endpoints() {
  echo
  echo "authmanager  profile=${PROFILE}  port=${PORT}  context=${CONTEXT}"
  echo "  health     ${BASE}/actuator/health"
  echo "  swagger    ${BASE}/swagger-ui/index.html"
  echo "  openapi    ${BASE}/v3/api-docs"
  echo "  info       ${BASE}/actuator/info"
  echo "  mappings   ${BASE}/actuator/mappings"
  echo "  prometheus ${BASE}/actuator/prometheus"
  echo "  token      POST ${BASE}/authenticate/clientidsecretkey"
  echo "  validate   GET  ${BASE}/authorize/admin/validateToken"
  echo "  refresh    POST ${BASE}/authorize/refreshToken/{appid}"
  echo "  invalidate POST ${BASE}/authorize/invalidateToken"
  echo
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

mvn_auth() {
  # Surefire inherits the env. local profile would take the deprecated offline path in adapter tests.
  (
    cd "$KERNEL_DIR"
    unset SPRING_PROFILES_ACTIVE || true
    mvn -pl "$MODULE" -am "$@"
  )
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
    if [[ "$(wc -c < "$jar")" -lt 1048576 ]]; then
      echo "error: ${jar} is not the Boot ZIP (no Main-Class). Run: $0 init" >&2
      exit 1
    fi
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
  echo "==> packaging ${MODULE} (skip tests)"
  mvn_auth clean package "${MVN_SKIP[@]}"
  echo "init complete"
}

cmd_test() {
  check_prereqs
  echo "==> maven tests"
  mvn_auth test "-Dgpg.skip=true" "-Dmaven.javadoc.skip=true"
}

cmd_start() {
  echo
  echo "==> run-local start"
  ensure_dirs
  check_prereqs
  if is_running; then
    echo "auth-service already running (pid $(cat "$PID_FILE")) on port ${PORT}"
    print_endpoints
    return 0
  fi
  rm -f "$LOG_FILE"
  local jar
  jar="$(find_boot_jar)"
  echo "==> starting auth-service from $(basename "$jar")"
  echo "    profile=${PROFILE}"
  echo "    port=${PORT}"
  echo "    context=${CONTEXT}"
  echo "    properties=src/main/resources/application-local.properties"
  if [[ -n "${CONFIG_URI}" ]]; then
    echo "    config.uri=${CONFIG_URI}"
  fi
  # JDK_JAVA_OPTIONS is read by the JVM; leave unset for local defaults (no cluster GC).
  local java_args=(
    "-Dspring.profiles.active=${PROFILE}"
    "-Dserver.port=${PORT}"
  )
  if [[ -n "${CONFIG_URI}" ]]; then
    java_args+=("-Dspring.cloud.config.uri=${CONFIG_URI}")
  fi
  if [[ -n "${CONFIG_LABEL}" ]]; then
    java_args+=("-Dspring.cloud.config.label=${CONFIG_LABEL}")
  fi
  java_args+=(-jar "$jar")
  nohup java "${java_args[@]}" >"$LOG_FILE" 2>&1 &
  echo $! >"$PID_FILE"
  echo "pid $(cat "$PID_FILE")  log ${LOG_FILE}"
  echo "==> waiting for Spring Boot startup on port ${PORT} (up to 90s) ..."
  wait_ready 90
  echo
  echo "auth-service ready"
  echo "  port    ${PORT}"
  print_endpoints
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
    if grep -q "Started AuthBootApplication" "$LOG_FILE" 2>/dev/null; then
      code="$(http_ok "${BASE}/actuator/health")"
      if [[ "$code" == "200" ]]; then
        return 0
      fi
    fi
    code="$(http_ok "${BASE}/actuator/health")"
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
    echo "auth-service is not running"
    return 0
  fi
  local pid
  pid="$(cat "$PID_FILE")"
  echo "==> stopping auth-service (${pid}) on port ${PORT}"
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
  echo "==> waiting for auth-service (up to ${timeout}s)"
  while [[ "$elapsed" -lt "$timeout" ]]; do
    if [[ -f "$PID_FILE" ]] && ! is_running; then
      echo "error: process exited. See ${LOG_FILE}" >&2
      tail -n 40 "$LOG_FILE" >&2 || true
      return 1
    fi
    local code
    code="$(http_ok "${BASE}/actuator/health")"
    if [[ "$code" == "200" ]]; then
      echo "healthy  ${BASE}/actuator/health"
      code="$(http_ok "${BASE}/swagger-ui/index.html")"
      echo "swagger  ${BASE}/swagger-ui/index.html  HTTP ${code}"
      if [[ "$code" != "200" ]]; then
        echo "error: swagger not healthy" >&2
        return 1
      fi
      echo "smoke ok"
      print_endpoints
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

docker_host_args() {
  case "$UNAME_S" in
    Linux*) echo --add-host=host.docker.internal:host-gateway ;;
  esac
}

cmd_docker() {
  cmd_init
  need_cmd docker
  docker rm -f kernel-auth-service >/dev/null 2>&1 || true
  echo "==> docker build ${IMAGE}"
  docker build -t "${IMAGE}" "${MODULE_DIR}"
  print_endpoints
  # shellcheck disable=SC2046
  exec docker run --rm -p "${PORT}:8091" --name kernel-auth-service \
    $(docker_host_args) \
    -e "active_profile_env=${PROFILE}" \
    -e "spring_config_url_env=${CONFIG_URI}" \
    -e "spring_config_label_env=${CONFIG_LABEL}" \
    -e "JDK_JAVA_OPTIONS=${JDK_JAVA_OPTIONS:-}" \
    "${IMAGE}"
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
    docker) cmd_docker ;;
    "") usage 1 ;;
    *) echo "error: unknown command '$cmd'" >&2; usage 1 ;;
  esac
}

main "$@"
