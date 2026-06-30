# Script de restauracion de un backup cifrado de la base de datos del SGA
#
# Uso:
#   .\scripts\restore-db.ps1 -Archivo "backups\sga_backup_2026-06-29_10-00-00.sql.enc"
#
# ADVERTENCIA: esto reemplaza todos los datos actuales de sga_db con los
# del backup. Si no estas seguro, haz un backup nuevo primero con backup-db.ps1.

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

# --- Descifrado AES-256 ---
$claveTexto = if ($env:SGA_BACKUP_KEY) { $env:SGA_BACKUP_KEY } else { "sga-unemi-backup-key-2026-desarrollo-local" }

Add-Type -AssemblyName System.Security

$aes = [System.Security.Cryptography.Aes]::Create()
$aes.Key = [System.Security.Cryptography.SHA256]::Create().ComputeHash([System.Text.Encoding]::UTF8.GetBytes($claveTexto))

$contenidoCifrado = [System.IO.File]::ReadAllBytes($Archivo)

# Los primeros 16 bytes son el IV guardado por backup-db.ps1
$iv = $contenidoCifrado[0..15]
$datosCifrados = $contenidoCifrado[16..($contenidoCifrado.Length - 1)]
$aes.IV = $iv

$decryptor = $aes.CreateDecryptor()
$contenidoDescifrado = $decryptor.TransformFinalBlock($datosCifrados, 0, $datosCifrados.Length)

$rutaTemporal = [System.IO.Path]::GetTempFileName()
[System.IO.File]::WriteAllBytes($rutaTemporal, $contenidoDescifrado)

Write-Host "Backup descifrado. Restaurando..."

Get-Content $rutaTemporal | docker exec -i sga_postgres psql -U postgres -d sga_db

Remove-Item $rutaTemporal

if ($LASTEXITCODE -eq 0) {
    Write-Host "Backup restaurado exitosamente."
} else {
    Write-Host "ERROR: Hubo un problema al restaurar el backup. Revisa los mensajes anteriores."
}