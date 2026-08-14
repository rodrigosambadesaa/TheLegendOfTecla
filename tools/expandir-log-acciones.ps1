param(
    [Parameter(Mandatory = $true)]
    [string]$Log,
    [Parameter(Mandatory = $true)]
    [string]$Salida
)

$ErrorActionPreference = "Stop"
$rutaLog = [IO.Path]::GetFullPath($Log)
$rutaSalida = [IO.Path]::GetFullPath($Salida)
if (-not (Test-Path -LiteralPath $rutaLog -PathType Leaf)) {
    throw "No existe el log comprimido: $rutaLog"
}
if (Test-Path -LiteralPath $rutaSalida) {
    throw "La salida ya existe: $rutaSalida"
}

function Resolver-Accion([string]$regla, [int]$id) {
    if ($regla -eq "IF id%12=0 THEN PRIORIZAR_BOTIQUIN ELSE EQUIPAR") {
        return $(if ($id % 12 -eq 0) { "PRIORIZAR_BOTIQUIN" } else { "EQUIPAR" })
    }
    if ($regla -eq "IF id%12=0 THEN MEDICAR ELSE IF id%5=0 THEN CUBRIR ELSE ATACAR") {
        if ($id % 12 -eq 0) { return "MEDICAR" }
        if ($id % 5 -eq 0) { return "CUBRIR" }
        return "ATACAR"
    }
    if ($regla -eq "IF id%4=0 THEN CUBRIR ELSE ATACAR_ARMA_PROPIA") {
        return $(if ($id % 4 -eq 0) { "CUBRIR" } else { "ATACAR_ARMA_PROPIA" })
    }
    return $regla.Replace("(position-v2)", "")
}

$entradaArchivo = [IO.File]::OpenRead($rutaLog)
$gzip = [IO.Compression.GzipStream]::new(
    $entradaArchivo, [IO.Compression.CompressionMode]::Decompress)
$lector = [IO.StreamReader]::new($gzip, [Text.Encoding]::UTF8)
$escritor = [IO.StreamWriter]::new($rutaSalida, $false, [Text.UTF8Encoding]::new($false))
$acciones = 0L
try {
    while (($linea = $lector.ReadLine()) -ne $null) {
        if ($linea.StartsWith("#")) {
            $escritor.WriteLine($linea)
            continue
        }
        $campos = $linea.Split('|')
        if ($campos.Count -ne 5) {
            throw "Linea de rango invalida: $linea"
        }
        $selector = $campos[1]
        if ($selector -eq "P") {
            $escritor.WriteLine(($campos[0..3] -join '|'))
            $acciones++
            continue
        }
        if ($selector -notmatch '^([AE])(\d{5})-\1(\d{5})$') {
            throw "Selector desconocido: $selector"
        }
        $bando = $Matches[1]
        $inicio = [int]$Matches[2]
        $fin = [int]$Matches[3]
        for ($id = $inicio; $id -le $fin; $id++) {
            $entidad = $bando + $id.ToString("00000")
            $accion = Resolver-Accion $campos[2] $id
            $escritor.WriteLine("$($campos[0])|$entidad|$accion|$($campos[3])")
            $acciones++
        }
    }
} finally {
    $escritor.Dispose()
    $lector.Dispose()
    $gzip.Dispose()
    $entradaArchivo.Dispose()
}

Write-Host "ACCIONES_EXPANDIDAS total=$acciones salida=$rutaSalida"
