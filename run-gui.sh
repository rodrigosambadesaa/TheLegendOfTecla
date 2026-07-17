#!/usr/bin/env bash
set -Eeuo pipefail

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$script_dir"

gui_port="${TECLA_GUI_PORT:-6080}"
gui_url="http://127.0.0.1:${gui_port}/vnc.html?autoconnect=1&resize=scale"
abrir_navegador=true
construir=true

mostrar_ayuda() {
    cat <<'EOF'
Uso: bash ./run-gui.sh [opciones]

Inicia la interfaz grafica Docker mediante un escritorio virtual y noVNC.
Funciona en Linux, WSL2 y macOS sin instalar ni configurar un servidor X.

  --no-open    No abre el navegador automaticamente
  --no-build   Reutiliza la imagen Docker existente
  --help       Muestra esta ayuda

Variable opcional:
  TECLA_GUI_PORT=6081 bash ./run-gui.sh
EOF
}

for argumento in "$@"; do
    case "$argumento" in
        --no-open) abrir_navegador=false ;;
        --no-build) construir=false ;;
        --help|-h)
            mostrar_ayuda
            exit 0
            ;;
        *)
            echo "Opcion desconocida: $argumento" >&2
            mostrar_ayuda >&2
            exit 2
            ;;
    esac
done

es_wsl=false
if [[ -r /proc/sys/kernel/osrelease ]] && grep -qi microsoft /proc/sys/kernel/osrelease; then
    es_wsl=true
fi

docker_cmd=()
if command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
    docker_cmd=(docker)
elif [[ "$es_wsl" == true ]] && command -v docker.exe >/dev/null 2>&1 \
        && docker.exe info >/dev/null 2>&1; then
    docker_cmd=(docker.exe)
    echo "Docker Desktop detectado desde WSL mediante docker.exe."
else
    echo "No se puede conectar con Docker." >&2
    if [[ "$es_wsl" == true ]]; then
        echo "Inicia Docker Desktop o activa Settings > Resources > WSL Integration." >&2
    else
        echo "Instala/inicia Docker Engine o Docker Desktop y vuelve a ejecutar el comando." >&2
    fi
    exit 1
fi

if ! "${docker_cmd[@]}" compose version >/dev/null 2>&1; then
    echo "Docker Compose v2 no esta disponible (se necesita 'docker compose')." >&2
    exit 1
fi

argumentos_compose=(up --detach)
if [[ "$construir" == true ]]; then
    argumentos_compose+=(--build)
fi
argumentos_compose+=(gui)

echo "Iniciando The Legend of Tecla GUI..."
TECLA_GUI_PORT="$gui_port" "${docker_cmd[@]}" compose "${argumentos_compose[@]}"

for intento in {1..90}; do
    if curl --fail --silent --show-error --max-time 2 \
            "http://127.0.0.1:${gui_port}/vnc.html" >/dev/null 2>&1; then
        echo "Interfaz grafica disponible en $gui_url"
        if [[ "$abrir_navegador" == true ]]; then
            if [[ "$(uname -s)" == "Darwin" ]]; then
                open "$gui_url"
            elif [[ "$es_wsl" == true ]] && command -v powershell.exe >/dev/null 2>&1; then
                powershell.exe -NoProfile -Command "Start-Process '$gui_url'" >/dev/null
            elif command -v xdg-open >/dev/null 2>&1; then
                xdg-open "$gui_url" >/dev/null 2>&1 || true
            else
                echo "No se encontro un lanzador de navegador; abre manualmente la URL anterior."
            fi
        fi
        exit 0
    fi
    sleep 1
done

echo "noVNC no respondio a tiempo. Ultimos registros:" >&2
TECLA_GUI_PORT="$gui_port" "${docker_cmd[@]}" compose logs --tail 80 gui >&2 || true
exit 1
