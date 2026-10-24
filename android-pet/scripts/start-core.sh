#!/data/data/com.termux/files/usr/bin/bash
set -u
cd "$HOME/Coopanion" || { echo "ERROR: ~/Coopanion not found"; exit 1; }
PID_FILE="$HOME/.coopanion-core.pid"
LOG_FILE="$HOME/.coopanion-core.log"

# Keep ownership information for a Core process started by this helper.
if [ -f "$PID_FILE" ]; then
  state="$(cat "$PID_FILE" 2>/dev/null || true)"
  case "$state" in
    managed:*)
      pid="${state#managed:}"
      if kill -0 "$pid" 2>/dev/null; then
        echo "ALREADY_RUNNING"
        exit 0
      fi
      ;;
  esac
fi

# If Core is already reachable but was started elsewhere, leave it alone on stop.
if node -e 'fetch("http://127.0.0.1:7797/pet").then(r=>process.exit(r.ok?0:1)).catch(()=>process.exit(1))' >/dev/null 2>&1; then
  echo "external" > "$PID_FILE"
  echo "ALREADY_RUNNING"
  exit 0
fi

nohup node --import tsx --expose-gc core/boot.ts >> "$LOG_FILE" 2>&1 </dev/null &
pid=$!
echo "managed:$pid" > "$PID_FILE"
echo "STARTED:$pid"
