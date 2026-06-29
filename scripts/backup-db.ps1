$fecha = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"
$carpetaBackups = "backups"
$nombreArchivo = "sga_backup_$fecha.sql"
$rutaCompleta = Join-Path $carpetaBackups $nombreArchivo

if (-not (Test-Path $carpetaBackups)) {
    New-Item -ItemType Directory -Path $carpetaBackups | Out-Null
    Write-Host "Carpeta '$carpetaBackups' creada."
}

Write-Host "Generando backup de la base de datos sga_db..."

docker exec sga_postgres pg_dump -U postgres -d sga_db --clean --if-exists > $rutaCompleta

if ((Test-Path $rutaCompleta) -and (Get-Item $rutaCompleta).Length -gt 0) {
    $tamano = (Get-Item $rutaCompleta).Length / 1KB
    Write-Host "Backup creado exitosamente: $rutaCompleta ($([math]::Round($tamano, 2)) KB)"
} else {
    Write-Host "ERROR: No se pudo generar el backup. Verifica que el contenedor sga_postgres este corriendo."
}

# Elimina backups de mas de 7 dias para no llenar el disco
$backupsViejos = Get-ChildItem -Path $carpetaBackups -Filter "sga_backup_*.sql" |
    Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-7) }

if ($backupsViejos.Count -gt 0) {
    Write-Host "Eliminando $($backupsViejos.Count) backup(s) de mas de 7 dias..."
    $backupsViejos | Remove-Item
}