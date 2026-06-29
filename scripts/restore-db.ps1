param(
    [Parameter(Mandatory=$true)]
    [string]$Archivo
)

if (-not (Test-Path $Archivo)) {
    Write-Host "ERROR: No se encuentra el archivo '$Archivo'."
    exit 1
}

Write-Host "ADVERTENCIA: esto va a reemplazar TODOS los datos actuales de sga_db."
$confirmacion = Read-Host "Escribe 'si' para confirmar"

if ($confirmacion -ne "si") {
    Write-Host "Operacion cancelada."
    exit 0
}

Write-Host "Restaurando backup desde '$Archivo'..."

Get-Content $Archivo | docker exec -i sga_postgres psql -U postgres -d sga_db

if ($LASTEXITCODE -eq 0) {
    Write-Host "Backup restaurado exitosamente."
} else {
    Write-Host "ERROR: Hubo un problema al restaurar el backup. Revisa los mensajes anteriores."
}