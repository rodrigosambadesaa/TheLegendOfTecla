#!/usr/bin/env bash
set -u

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
bash "$script_dir/run-gui.sh" "$@"
estado=$?

if [[ $estado -ne 0 && -t 0 ]]; then
    echo
    read -r -p "El inicio ha fallado. Pulsa ENTER para cerrar..." _
fi
exit "$estado"
