#!/data/data/com.termux/files/usr/bin/bash
set -u
PID_FILE="$HOME/.coopanion-core.pid"
if [ ! -f "$PID_FILE" ]; then
  echo "NO_MANAGED_CORE"
  exit 0
fi
state="$(cat "$PID_FILE" 2>/dev/null || true)"
case "$state" in
  managed:*)
    pid="${state#managed:}"
    if kill -0 "$pid" 2>/dev/null; then
      kill "$pid" 2>/dev/null || true
      echo "STOP_REQUESTED:$pid"
    else
      echo "ALREADY_STOPPED"
    fi
    ;;
  external)
    echo "CORE_WAS_ALREADY_RUNNING_NOT_STOPPED"
    ;;
  *)
    echo "UNKNOWN_PID_FILE_STATE"
    ;;
esac
rm -f "$PID_FILE"
