$ErrorActionPreference = "Stop"

$guiUrl = "http://localhost:6080/vnc.html?autoconnect=1&resize=scale"

docker compose up --build --detach gui
if ($LASTEXITCODE -ne 0) {
    throw "No se pudo iniciar el contenedor de la interfaz grafica."
}

for ($attempt = 0; $attempt -lt 60; $attempt++) {
    try {
        Invoke-WebRequest -Uri "http://localhost:6080/vnc.html" -UseBasicParsing -TimeoutSec 2 | Out-Null
        Start-Process $guiUrl
        Write-Host "Interfaz grafica disponible en $guiUrl"
        exit 0
    } catch {
        Start-Sleep -Seconds 1
    }
}

throw "El contenedor se inicio, pero noVNC no respondio a tiempo. Revisa: docker compose logs gui"
