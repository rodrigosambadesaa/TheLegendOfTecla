param(
    [switch]$OmitirCompilacion
)

$ErrorActionPreference = "Stop"
$raizProyecto = Split-Path -Parent $PSScriptRoot
$directorioRuns = Join-Path $raizProyecto "docs\runs"
$directorioMatriz = Join-Path $directorioRuns "dificultad_matrix"
$jarJuego = Join-Path $raizProyecto "target\the-legend-of-tecla.jar"
$utf8SinBom = [Text.UTF8Encoding]::new($false)
$salidaAnterior = $OutputEncoding
$colorAnterior = $env:NO_COLOR
$OutputEncoding = $utf8SinBom
$env:NO_COLOR = "1"

New-Item -ItemType Directory -Force -Path $directorioMatriz | Out-Null

function Normalizar-Registro([string]$ruta) {
    $contenido = [IO.File]::ReadAllText($ruta)
    $contenido = $contenido.TrimStart([char]0xFEFF)
    $patronAnsi = [string][char]27 + '\[[0-?]*[ -/]*[@-~]'
    $contenido = [regex]::Replace($contenido, $patronAnsi, "")
    $contenido = $contenido -replace "`r?`n", "`n"
    $contenido = [regex]::Replace($contenido, '[ \t]+(?=\n|$)', "")
    if (-not $contenido.StartsWith("# tecla-run-log-v2")) {
        $cabecera = "# tecla-run-log-v2`n# source: current-java17-engine`n" +
            "# encoding: UTF-8`n# ansi: removed`n"
        $contenido = $cabecera + $contenido
    }
    Guardar-Registro $ruta @($contenido.TrimEnd())
}

function Guardar-Registro([string]$ruta, [object[]]$lineas) {
    $contenido = ($lineas | ForEach-Object { [string]$_ }) -join "`n"
    for ($intento = 1; $intento -le 12; $intento++) {
        try {
            [IO.File]::WriteAllText($ruta, $contenido.TrimEnd() + "`n", $utf8SinBom)
            return
        } catch [IO.IOException] {
            if ($intento -eq 12) { throw }
            Start-Sleep -Milliseconds (100 * $intento)
        }
    }
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
    $salidaBase = $comandosBase | & java -jar $jarJuego --rapido --nombre Tecla --clase marine `
        --modo default --dificultad normal --dimensiones 10x10 --aliados no
    Guardar-Registro (Join-Path $directorioRuns "partida_normal.log") $salidaBase

    $comandosAliados = "ayuda`nreagrupar defensiva`nmirar`nreagrupar ofensiva`ninventario`nsalir`n"
    $salidaAliados = $comandosAliados | & java -jar $jarJuego --rapido --nombre Tecla --clase marine `
        --modo grande --dificultad normal --dimensiones 22x24 --aliados si `
        --victoria jugador_y_aliados --variante 7
    Guardar-Registro (Join-Path $directorioRuns "partida_con_aliados.log") $salidaAliados

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
                $salidaMatriz = "salir`n" | & java @argumentos
                if ($LASTEXITCODE -ne 0) {
                    throw "Fallo en la matriz: $modo / $dificultad / $($perfil.Nombre)."
                }
                Guardar-Registro (Join-Path $directorioMatriz $nombre) $salidaMatriz
            }
        }
    }

    Get-ChildItem -LiteralPath $directorioRuns -Recurse -File -Filter "*.log" |
        ForEach-Object { Normalizar-Registro $_.FullName }
} finally {
    Pop-Location
    $OutputEncoding = $salidaAnterior
    if ($null -eq $colorAnterior) {
        Remove-Item Env:NO_COLOR -ErrorAction SilentlyContinue
    } else {
        $env:NO_COLOR = $colorAnterior
    }
}

Write-Host "Evidencias regeneradas en $directorioRuns"
