#!/bin/sh
set -eu

display_number="${DISPLAY:-:99}"

cleanup() {
    for pid in ${app_pid:-} ${websockify_pid:-} ${vnc_pid:-} ${fluxbox_pid:-} ${xvfb_pid:-}; do
        if [ -n "$pid" ]; then
            kill "$pid" 2>/dev/null || true
        fi
    done
}
trap cleanup EXIT INT TERM

Xvfb "$display_number" -screen 0 1280x800x24 -ac -nolisten tcp +extension RANDR &
xvfb_pid=$!

attempt=0
until xdpyinfo -display "$display_number" >/dev/null 2>&1; do
    attempt=$((attempt + 1))
    if [ "$attempt" -ge 50 ]; then
        echo "No se pudo iniciar el escritorio virtual." >&2
        exit 1
    fi
    sleep 0.1
done

fluxbox >/tmp/fluxbox.log 2>&1 &
fluxbox_pid=$!

x11vnc -display "$display_number" -localhost -forever -shared -nopw \
    -rfbport 5900 -quiet >/tmp/x11vnc.log 2>&1 &
vnc_pid=$!

websockify --web=/usr/share/novnc/ 6080 127.0.0.1:5900 \
    >/tmp/websockify.log 2>&1 &
websockify_pid=$!

java -Dfile.encoding=UTF-8 -jar /app/game.jar "$@" &
app_pid=$!
wait "$app_pid"
