# ============================================================
# NordTafl - Script de arranque
# Carga variables del .env y ejecuta el backend
# ============================================================

$ErrorActionPreference = "Stop"

# 1. Verificar que existe el .env
$envFile = Join-Path $PSScriptRoot ".env"
if (-not (Test-Path $envFile)) {
    Write-Host "ERROR: No se encontro el archivo .env en $envFile" -ForegroundColor Red
    Write-Host "Copia .env.example a .env y rellena los valores." -ForegroundColor Yellow
    exit 1
}

# 2. Cargar variables del .env
Write-Host "Cargando variables del .env..." -ForegroundColor Cyan
$lineNumber = 0
Get-Content $envFile -Encoding UTF8 | ForEach-Object {
    $lineNumber++
    $line = $_.Trim()
    
    if ($line -eq "" -or $line.StartsWith("#")) {
        return
    }
    
    if ($line -match "^([^=]+)=(.*)$") {
        $key = $matches[1].Trim()
        $value = $matches[2].Trim()
        
        $value = $value -replace '^"(.*)"$', '$1'
        $value = $value -replace "^'(.*)'$", '$1'
        
        [System.Environment]::SetEnvironmentVariable($key, $value, "Process")
        Write-Host "  $key cargada" -ForegroundColor DarkGray
    }
    else {
        Write-Host "  Linea $lineNumber ignorada: $line" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Variables cargadas correctamente." -ForegroundColor Green
Write-Host ""

# 3. Arrancar el backend
Write-Host "Arrancando NordTafl Backend..." -ForegroundColor Cyan
Write-Host ""

& "$PSScriptRoot\gradlew.bat" bootRun
