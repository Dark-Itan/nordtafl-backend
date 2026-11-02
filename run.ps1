$ErrorActionPreference = "Stop"

# 1. Verificar que existe el .env
$envFile = Join-Path $PSScriptRoot ".env"
if (-not (Test-Path $envFile)) {
    Write-Host "ERROR: No se encontro el archivo .env en $envFile" -ForegroundColor Red
    Write-Host "Copia .env.example a .env y rellena los valores." -ForegroundColor Yellow
    exit 1
}

# 2. Cargar variables del .env y preparar los argumentos para Gradle
Write-Host "Cargando variables del .env..." -ForegroundColor Cyan
$lineNumber = 0
$envArgs = @()

Get-Content $envFile -Encoding UTF8 | ForEach-Object {
    $lineNumber++
    $line = $_.Trim()

    # Ignorar lineas vacias y comentarios
    if ($line -eq "" -or $line.StartsWith("#")) {
        return
    }

    # Parsear KEY=VALUE
    if ($line -match "^([^=]+)=(.*)$") {
        $key = $matches[1].Trim()
        $value = $matches[2].Trim()

        # Quitar comillas si las tiene
        $value = $value -replace '^"(.*)"$', '$1'
        $value = $value -replace "^'(.*)'$", '$1'

        # 1. Inyectar en el proceso actual (por si acaso)
        [System.Environment]::SetEnvironmentVariable($key, $value, "Process")

        # 2. Preparar argumento para Gradle/Java (-DKEY=VALUE)
        $envArgs += "-D$key=$value"

        Write-Host "  $key cargada" -ForegroundColor DarkGray
    }
    else {
        Write-Host "  Linea $lineNumber ignorada: $line" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "Variables cargadas correctamente." -ForegroundColor Green
Write-Host ""

# 3. Arrancar el backend pasando las variables a Gradle
Write-Host "Arrancando NordTafl Backend..." -ForegroundColor Cyan
Write-Host ""

# Ejecutar Gradle pasando los argumentos -D
& "$PSScriptRoot\gradlew.bat" bootRun @envArgs