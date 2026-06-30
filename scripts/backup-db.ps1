# Script de backup para la base de datos del SGA, con cifrado y
# almacenamiento aislado (RNF-0010: Políticas de Respaldo de Información).
#
# El dump se cifra con AES-256 antes de guardarse, y ademas se sube a un
# bucket de MinIO separado ("backups-respaldo", distinto del bucket de
# recursos pedagogicos), cumpliendo el criterio de "servicio aislado de la
# app principal".
#
# Uso:
#   .\scripts\backup-db.ps1
#
# IMPORTANTE: la clave de cifrado se toma de la variable de entorno
# SGA_BACKUP_KEY si existe; si no, usa una clave por defecto SOLO para
# desarrollo local. En un despliegue real, definir SGA_BACKUP_KEY como
# variable de entorno del servidor, nunca dejarla hardcodeada en el script.

$fecha = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"
$carpetaBackups = "backups"
$nombreSinCifrar = "sga_backup_$fecha.sql"
$nombreCifrado = "sga_backup_$fecha.sql.enc"
$rutaSinCifrar = Join-Path $carpetaBackups $nombreSinCifrar
$rutaCifrada = Join-Path $carpetaBackups $nombreCifrado

if (-not (Test-Path $carpetaBackups)) {
    New-Item -ItemType Directory -Path $carpetaBackups | Out-Null
    Write-Host "Carpeta '$carpetaBackups' creada."
}

Write-Host "Generando backup de la base de datos sga_db..."
docker exec sga_postgres pg_dump -U postgres -d sga_db --clean --if-exists > $rutaSinCifrar

if (-not ((Test-Path $rutaSinCifrar) -and (Get-Item $rutaSinCifrar).Length -gt 0)) {
    Write-Host "ERROR: No se pudo generar el backup. Verifica que el contenedor sga_postgres este corriendo."
    exit 1
}

# --- Cifrado AES-256 (usando System.Security.Cryptography, nativo de .NET) ---
$claveTexto = if ($env:SGA_BACKUP_KEY) { $env:SGA_BACKUP_KEY } else { "sga-unemi-backup-key-2026-desarrollo-local" }

Add-Type -AssemblyName System.Security

$aes = [System.Security.Cryptography.Aes]::Create()
$aes.Key = [System.Security.Cryptography.SHA256]::Create().ComputeHash([System.Text.Encoding]::UTF8.GetBytes($claveTexto))
$aes.GenerateIV()

$contenido = [System.IO.File]::ReadAllBytes($rutaSinCifrar)
$encryptor = $aes.CreateEncryptor()
$contenidoCifrado = $encryptor.TransformFinalBlock($contenido, 0, $contenido.Length)

# El IV se guarda al inicio del archivo cifrado (necesario para descifrar despues)
$flujoSalida = [System.IO.File]::Create($rutaCifrada)
$flujoSalida.Write($aes.IV, 0, $aes.IV.Length)
$flujoSalida.Write($contenidoCifrado, 0, $contenidoCifrado.Length)
$flujoSalida.Close()

Remove-Item $rutaSinCifrar  # elimina la copia sin cifrar, solo queda la cifrada

$tamano = (Get-Item $rutaCifrada).Length / 1KB
Write-Host "Backup cifrado creado exitosamente: $rutaCifrada ($([math]::Round($tamano, 2)) KB)"

# --- Subida a MinIO en un bucket aislado (servicio separado de la app) ---
Write-Host "Subiendo copia cifrada a MinIO (bucket aislado 'backups-respaldo')..."

docker exec sga_minio mc alias set local http://localhost:9000 minioadmin minioadmin123 2>$null
docker exec sga_minio mc mb --ignore-existing local/backups-respaldo 2>$null
docker cp $rutaCifrada sga_minio:/tmp/$nombreCifrado
docker exec sga_minio mc cp /tmp/$nombreCifrado local/backups-respaldo/$nombreCifrado

if ($LASTEXITCODE -eq 0) {
    Write-Host "Copia subida exitosamente a MinIO: backups-respaldo/$nombreCifrado"
} else {
    Write-Host "ADVERTENCIA: no se pudo subir la copia a MinIO. El backup cifrado local sigue disponible en '$rutaCifrada'."
}

# Elimina backups locales de mas de 7 dias para no llenar el disco
$backupsViejos = Get-ChildItem -Path $carpetaBackups -Filter "sga_backup_*.sql.enc" |
    Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-7) }

if ($backupsViejos.Count -gt 0) {
    Write-Host "Eliminando $($backupsViejos.Count) backup(s) local(es) de mas de 7 dias..."
    $backupsViejos | Remove-Item
}