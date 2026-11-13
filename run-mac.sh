#!/usr/bin/env bash
# =============================================================================
#  Adventure Book - start the API, the Angular app and the React app (macOS)
#
#    ./run-mac.sh [start | check] [api] [angular] [react]
#
#    start   (default) start the servers in this terminal; Ctrl+C stops them all
#    check   start the servers, wait until they all answer, then stop them
#
#  Without service names, all three are used. Example, when the API already
#  runs in your IDE:   ./run-mac.sh start angular react
#
#  Also works on Linux. Compatible with the bash 3.2 shipped with macOS.
# =============================================================================
set -u

ROOT="$(cd "$(dirname "$0")" && pwd)"
API_DIR="$ROOT/adventure-book-api"
ANGULAR_DIR="$ROOT/adventure-book-front-angular"
REACT_DIR="$ROOT/adventure-book-front-react"
API_JAR="$API_DIR/target/adventure-book-api-0.0.1-SNAPSHOT.jar"
WAIT_SECONDS="${WAIT_SECONDS:-300}"

if [ -t 1 ]; then USE_COLOR=1; else USE_COLOR=0; fi

paint() { # <ansi color> <text>
  if [ "$USE_COLOR" = 1 ]; then printf '\033[%sm%s\033[0m' "$1" "$2"; else printf '%s' "$2"; fi
}
log()  { printf '%s %s\n' "$(paint '1;35' '    run |')" "$*"; }
warn() { log "$(paint '1;33' "WARNING: $*")"; }
die()  { log "$(paint '1;31' "ERROR: $*")"; exit 1; }

usage() {
  cat <<'EOF'
Usage: ./run-mac.sh [start|check] [api] [angular] [react]

  start   (default) start the servers in this terminal; Ctrl+C stops them all
  check   start the servers, wait until they all answer, then stop them

  Without service names, all three are used.
  Example (API already running in your IDE): ./run-mac.sh start angular react

  API      http://localhost:8080/api  (Maven if on PATH, else the built jar)
  Angular  http://localhost:4200
  React    http://localhost:3000
EOF
}

# ----------------------------------------------------------------- arguments
ACTION=start
RUN_API=
RUN_ANGULAR=
RUN_REACT=
for arg in "$@"; do
  case "$arg" in
    start|check) ACTION="$arg" ;;
    api) RUN_API=1 ;;
    angular) RUN_ANGULAR=1 ;;
    react) RUN_REACT=1 ;;
    help|-h|--help) usage; exit 0 ;;
    *) usage; echo; die "unknown argument \"$arg\"." ;;
  esac
done
if [ -z "$RUN_API$RUN_ANGULAR$RUN_REACT" ]; then
  RUN_API=1; RUN_ANGULAR=1; RUN_REACT=1
fi

# ----------------------------------------------------------------- checks
command -v curl >/dev/null 2>&1 || die "curl not found."

if [ -n "$RUN_ANGULAR$RUN_REACT" ]; then
  command -v npm >/dev/null 2>&1 \
    || die "npm not found. Install Node.js 20 or 22 LTS (https://nodejs.org or 'brew install node@22')."
  node_major=$(node -v | sed -E 's/^v([0-9]+).*/\1/')
  [ "$node_major" -ge 18 ] 2>/dev/null || die "Node.js 18.19 or newer is required, found $(node -v)."
fi

if [ -n "$RUN_API" ]; then
  JAVA=java
  if [ -n "${JAVA_HOME:-}" ] && [ -x "$JAVA_HOME/bin/java" ]; then JAVA="$JAVA_HOME/bin/java"; fi
  java_version=$("$JAVA" -version 2>&1 | head -n 1)
  java_major=$(printf '%s' "$java_version" | sed -nE 's/.*version "([0-9]+).*/\1/p')
  [ -n "$java_major" ] \
    || die "Java not found. Install JDK 21 ('brew install openjdk@21') and set JAVA_HOME or add it to PATH."
  [ "$java_major" -ge 21 ] || die "JDK 21 is required, found: $java_version"
fi

port_busy() { # <port>
  if command -v lsof >/dev/null 2>&1; then
    lsof -nP -iTCP:"$1" -sTCP:LISTEN >/dev/null 2>&1
  elif command -v nc >/dev/null 2>&1; then
    nc -z localhost "$1" >/dev/null 2>&1
  else
    return 1
  fi
}
busy=
if [ -n "$RUN_API" ] && port_busy 8080; then log "$(paint '1;31' 'ERROR: port 8080 (API) is already in use.')"; busy=1; fi
if [ -n "$RUN_ANGULAR" ] && port_busy 4200; then log "$(paint '1;31' 'ERROR: port 4200 (Angular) is already in use.')"; busy=1; fi
if [ -n "$RUN_REACT" ] && port_busy 3000; then log "$(paint '1;31' 'ERROR: port 3000 (React) is already in use.')"; busy=1; fi
[ -z "$busy" ] || die "Stop whatever uses it first (another run of this script, or the server running in your IDE)."

if [ -n "$RUN_API" ]; then
  if command -v mvn >/dev/null 2>&1; then
    # Maven compiles the sources: Lombok 1.18.38 (managed by Spring Boot 3.5.5) needs JDK 21.
    if [ "$java_major" != 21 ]; then
      log "$(paint '1;31' "ERROR: building the API with Maven needs JDK 21, but $java_version was found.")"
      log "       Lombok does not run on newer JDKs. Point JAVA_HOME to a JDK 21, for example:"
      log "       export JAVA_HOME=\$(/usr/libexec/java_home -v 21)"
      exit 1
    fi
    API_CMD=(mvn spring-boot:run)
    log "API: Maven found, running from sources (mvn spring-boot:run)."
  elif [ -f "$API_JAR" ]; then
    if [ -n "$(find "$API_DIR/src/main" -type f -newer "$API_JAR" 2>/dev/null | head -n 1)" ]; then
      warn "the API jar is older than the API sources, so recent changes (e.g. CORS) may be missing."
      log "         Rebuild it with 'mvn package' in adventure-book-api, or the Maven 'package' goal in your IDE."
    fi
    API_CMD=("$JAVA" -jar "$API_JAR")
    log "API: Maven not found, running the built jar with java -jar."
  else
    die "Maven is not on the PATH and the API jar does not exist ($API_JAR). Install Maven 3.9+ ('brew install maven') or build the jar in your IDE."
  fi
fi

install_if_needed() { # <dir> <label>
  [ -d "$1/node_modules" ] && return 0
  log "Installing $2 dependencies (npm ci)..."
  (cd "$1" && npm ci) || die "npm ci failed for $2."
}
if [ -n "$RUN_ANGULAR" ]; then install_if_needed "$ANGULAR_DIR" Angular; fi
if [ -n "$RUN_REACT" ]; then install_if_needed "$REACT_DIR" React; fi

# ----------------------------------------------------------------- processes
PIDS=

prefix_output() { # <name> <ansi color>: prints every line as "   name | line"
  local label line
  label=$(paint "$2" "$(printf '%7s |' "$1")")
  while IFS= read -r line || [ -n "$line" ]; do
    printf '%s %s\n' "$label" "$line"
  done
}

start_service() { # <name> <ansi color> <dir> <command...>
  local name=$1 col=$2 dir=$3 pid
  shift 3
  # Job control gives each server its own process group: Ctrl+C only reaches this
  # script, which then stops every group (npm/mvn and the node/java they spawn).
  set -m
  ( cd "$dir" && exec "$@" ) < /dev/null > >(prefix_output "$name" "$col") 2>&1 &
  pid=$!
  set +m
  eval "PID_$name=$pid"
  PIDS="$PIDS $pid"
  log "Started $name (pid $pid): $*"
}

any_alive() {
  local pid
  for pid in $PIDS; do kill -0 "$pid" 2>/dev/null && return 0; done
  return 1
}

cleanup() {
  trap - INT TERM EXIT
  [ -n "$PIDS" ] || return 0
  log "Stopping all servers..."
  local pid i
  for pid in $PIDS; do kill -TERM -- "-$pid" 2>/dev/null || kill -TERM "$pid" 2>/dev/null; done
  for i in 1 2 3 4 5 6 7 8 9 10; do
    any_alive || break
    sleep 1
  done
  for pid in $PIDS; do kill -KILL -- "-$pid" 2>/dev/null; done
  wait 2>/dev/null
  log "All servers stopped."
}
trap 'echo; cleanup; exit 130' INT TERM
trap cleanup EXIT

wait_ready() { # <label> <url> <pid>
  local label=$1 url=$2 pid=$3 waited=0
  log "Waiting for $label at $url ..."
  until curl -sf -o /dev/null --max-time 3 "$url"; do
    if ! kill -0 "$pid" 2>/dev/null; then
      log "$(paint '1;31' "ERROR: $label stopped before it was ready (see its output above).")"
      return 1
    fi
    if [ "$waited" -ge "$WAIT_SECONDS" ]; then
      log "$(paint '1;31' "ERROR: $label did not answer within $WAIT_SECONDS seconds.")"
      return 1
    fi
    if [ "$waited" -gt 0 ] && [ $((waited % 30)) -eq 0 ]; then log "Still waiting for $label (${waited}s)..."; fi
    sleep 2
    waited=$((waited + 2))
  done
  log "$(paint '1;32' "$label is ready.")"
}

# ----------------------------------------------------------------- start
if [ -n "$RUN_API" ]; then start_service api 33 "$API_DIR" "${API_CMD[@]}"; fi
if [ -n "$RUN_ANGULAR" ]; then start_service angular 31 "$ANGULAR_DIR" npm start; fi
# BROWSER=none: do not let create-react-app open a browser tab.
if [ -n "$RUN_REACT" ]; then start_service react 36 "$REACT_DIR" env BROWSER=none npm start; fi

failed=
if [ -n "$RUN_API" ]; then wait_ready API "http://localhost:8080/api/actuator/health" "$PID_api" || failed=1; fi
if [ -z "$failed" ] && [ -n "$RUN_ANGULAR" ]; then wait_ready Angular "http://localhost:4200/" "$PID_angular" || failed=1; fi
if [ -z "$failed" ] && [ -n "$RUN_REACT" ]; then wait_ready React "http://localhost:3000/" "$PID_react" || failed=1; fi
[ -z "$failed" ] || exit 1

echo
log "$(paint '1;32' 'All servers are ready:')"
if [ -n "$RUN_API" ]; then log "  API          http://localhost:8080/api/books"; fi
if [ -n "$RUN_ANGULAR" ]; then log "  Angular app  http://localhost:4200"; fi
if [ -n "$RUN_REACT" ]; then log "  React app    http://localhost:3000"; fi

if [ "$ACTION" = check ]; then
  log "check: stopping the servers."
  exit 0
fi

log "Press Ctrl+C to stop all servers."
while :; do
  for pid in $PIDS; do
    if ! kill -0 "$pid" 2>/dev/null; then
      log "$(paint '1;31' 'ERROR: a server stopped unexpectedly (see its output above).')"
      exit 1
    fi
  done
  sleep 2
done
