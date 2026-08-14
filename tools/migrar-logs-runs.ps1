param(
    [string]$Directorio = ""
)

$ErrorActionPreference = "Stop"
$raizProyecto = Split-Path -Parent $PSScriptRoot
$directorioRuns = if ([string]::IsNullOrWhiteSpace($Directorio)) {
    Join-Path $raizProyecto "docs\runs"
} else {
    [IO.Path]::GetFullPath($Directorio)
}
$raizPermitida = [IO.Path]::GetFullPath((Join-Path $raizProyecto "docs\runs"))
$destinoResuelto = [IO.Path]::GetFullPath($directorioRuns)
if (-not $destinoResuelto.StartsWith($raizPermitida, [StringComparison]::OrdinalIgnoreCase)) {
    throw "El migrador solo puede modificar logs dentro de docs/runs."
}

$utf8SinBom = [Text.UTF8Encoding]::new($false)
$migrados = 0
Get-ChildItem -LiteralPath $destinoResuelto -Recurse -File -Filter "*.log" | ForEach-Object {
    $contenido = [IO.File]::ReadAllText($_.FullName)
    $contenido = $contenido.TrimStart([char]0xFEFF)
    $patronAnsi = [string][char]27 + '\[[0-?]*[ -/]*[@-~]'
    $contenido = [regex]::Replace($contenido, $patronAnsi, "")
    $contenido = $contenido -replace "`r?`n", "`n"
    $contenido = [regex]::Replace($contenido, '[ \t]+(?=\n|$)', "")
    $contenido = [regex]::Replace($contenido,
        "Comando desconocido: \?([A-Za-z])", "Comando desconocido: `$1")
    if (-not $contenido.StartsWith("# tecla-run-log-v2")) {
        $cabecera = "# tecla-run-log-v2`n# migrated-from: legacy-console-log`n" +
            "# encoding: UTF-8`n# ansi: removed`n"
        $contenido = $cabecera + $contenido
    }
    [IO.File]::WriteAllText($_.FullName, $contenido.TrimEnd() + "`n", $utf8SinBom)
    $migrados++
}

Write-Host "LOGS_MIGRADOS total=$migrados directorio=$destinoResuelto"
