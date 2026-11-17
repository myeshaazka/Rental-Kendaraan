# =====================================================================
# Compile + jalankan aplikasi tanpa Eclipse.
# Pakai: powershell -ExecutionPolicy Bypass -File .\run.ps1
# =====================================================================
$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $root

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

$sources = @(Get-ChildItem -Recurse -Filter *.java -Path "src" | ForEach-Object { $_.FullName })
if ($sources.Length -eq 0) {
    Write-Host "Tidak ada file .java di folder src." -ForegroundColor Red
    exit 1
}

Write-Host "Mengompilasi $($sources.Length) file Java..." -ForegroundColor Cyan
javac -encoding UTF-8 -d bin -cp "lib\postgresql-42.7.4.jar" $sources
if ($LASTEXITCODE -ne 0) {
    Write-Host "Kompilasi gagal. Periksa pesan error di atas." -ForegroundColor Red
    exit $LASTEXITCODE
}

# Salin konfigurasi agar terbaca dari classpath (bin) saat aplikasi dijalankan.
Copy-Item -Path "src\db.properties" -Destination "bin\db.properties" -Force

Write-Host "Menjalankan Sistem Manajemen Rental Kendaraan..." -ForegroundColor Green
java -cp "bin;lib\postgresql-42.7.4.jar" com.rentalkendaraan.Main
