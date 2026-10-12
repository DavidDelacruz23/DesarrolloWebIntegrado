# Lanzador para principiantes. Ejecuta este archivo desde PowerShell dentro de la carpeta del proyecto.
param([switch]$Demo)
$ErrorActionPreference = 'Stop'
# Read-Host evita escribir la contraseña en un archivo del proyecto o en el historial de comandos.
if (-not $env:DB_USER) { $env:DB_USER = Read-Host 'Usuario de MySQL (por ejemplo root)' }
if (-not $env:DB_URL) { $env:DB_URL = 'jdbc:mysql://localhost:3306/vet_citas_db?createDatabaseIfNotExist=true&serverTimezone=America/Lima' }
if ($null -eq $env:DB_PASSWORD) {
    $claveSegura = Read-Host 'Contraseña de MySQL (puedes dejarla vacía si tu instalación lo permite)' -AsSecureString
    $puntero = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($claveSegura)
    try { $env:DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($puntero) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($puntero) }
}
Push-Location (Join-Path $PSScriptRoot 'backend')
try {
    # El wrapper descarga Maven la primera vez; solo se necesita instalar Java 17 o posterior.
    if ($Demo) { & '.\mvnw.cmd' spring-boot:run '-Dspring-boot.run.profiles=demo' }
    else { & '.\mvnw.cmd' spring-boot:run }
} finally { Pop-Location }
