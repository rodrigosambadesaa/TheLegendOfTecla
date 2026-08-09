param(
    [switch]$OmitirCompilacion
)

$ErrorActionPreference = "Stop"
$raizProyecto = Split-Path -Parent $PSScriptRoot
$directorioRuns = Join-Path $raizProyecto "docs\runs"
$directorioMatriz = Join-Path $directorioRuns "dificultad_matrix"
$jarJuego = Join-Path $raizProyecto "target\the-legend-of-tecla.jar"

New-Item -ItemType Directory -Force -Path $directorioMatriz | Out-Null

function Normalizar-Registro([string]$ruta) {
    $lineas = [IO.File]::ReadAllLines($ruta) | ForEach-Object { $_.TrimEnd() }
    [IO.File]::WriteAllLines($ruta, $lineas, [Text.UTF8Encoding]::new($false))
}

Push-Location $raizProyecto
try {
    if (-not $OmitirCompilacion) {
        $registroSuite = Join-Path $directorioRuns "suite_completa.log"
        & mvn verify | Tee-Object -FilePath $registroSuite
        if ($LASTEXITCODE -ne 0) {
            throw "La suite Maven no ha terminado correctamente."
        }
        Normalizar-Registro $registroSuite
    }

    $registroCompuestos = Join-Path $directorioRuns "comandos_compuestos_consola_gui.log"
    & mvn '-Denforcer.skip=true' `
        '-Dtest=ComandosCompuestosConsolaGuiTest,ReglasCombateYVictoriaTest,IntegracionModosGuiTest,FormacionesAliadasTest' `
        test | Tee-Object -FilePath $registroCompuestos
    if ($LASTEXITCODE -ne 0) {
        throw "Las pruebas de comandos compuestos y equivalencia GUI/consola han fallado."
    }
    Normalizar-Registro $registroCompuestos

    $comandosBase = "ayuda`nmirar`ninventario`ndescansar`nsalir`n"
    $comandosBase | & java -jar $jarJuego --rapido --nombre Tecla --clase marine `
        --modo default --dificultad normal --dimensiones 10x10 --aliados no |
        Set-Content -Encoding utf8 (Join-Path $directorioRuns "partida_normal.log")

    $comandosAliados = "ayuda`nreagrupar defensiva`nmirar`nreagrupar ofensiva`ninventario`nsalir`n"
    $comandosAliados | & java -jar $jarJuego --rapido --nombre Tecla --clase marine `
        --modo grande --dificultad normal --dimensiones 22x24 --aliados si `
        --victoria jugador_y_aliados --variante 7 |
        Set-Content -Encoding utf8 (Join-Path $directorioRuns "partida_con_aliados.log")

    $modos = @("default", "grande", "ficheros")
    $dificultades = @("muy_facil", "facil", "normal", "dificil", "muy_dificil", "pesadilla", "demente")
    $perfiles = @(
        @{ Nombre = "base"; Dimensiones = "10x10" },
        @{ Nombre = "rectangular"; Dimensiones = "22x24" }
    )

    foreach ($modo in $modos) {
        foreach ($dificultad in $dificultades) {
            foreach ($perfil in $perfiles) {
                $indiceModo = $modos.IndexOf($modo) + 1
                $nombre = "modo$($indiceModo)_$($dificultad)_$($perfil.Nombre).log"
                $argumentos = @(
                    "-jar", $jarJuego, "--rapido", "--nombre", "Matriz",
                    "--clase", "marine", "--modo", $modo, "--dificultad", $dificultad,
                    "--dimensiones", $perfil.Dimensiones, "--aliados", "no"
                )
                if ($modo -eq "ficheros") {
                    $argumentos += @("--datos", (Join-Path $raizProyecto "data\escenario_basico"))
                }
                "salir`n" | & java @argumentos |
                    Set-Content -Encoding utf8 (Join-Path $directorioMatriz $nombre)
                if ($LASTEXITCODE -ne 0) {
                    throw "Fallo en la matriz: $modo / $dificultad / $($perfil.Nombre)."
                }
            }
        }
    }

    Get-ChildItem -LiteralPath $directorioRuns -Recurse -File -Filter "*.log" |
        ForEach-Object { Normalizar-Registro $_.FullName }
} finally {
    Pop-Location
}

Write-Host "Evidencias regeneradas en $directorioRuns"
