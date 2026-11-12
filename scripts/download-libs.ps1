# PowerShell script to download external JAR dependencies for BlindHire
$ErrorActionPreference = "Stop"
$libDir = Join-Path $PSScriptRoot "..\backend\lib"

if (-not (Test-Path $libDir)) {
    New-Item -ItemType Directory -Path $libDir -Force | Out-Null
}

$libraries = @(
    @{ Name = "mysql-connector-j-8.3.0.jar"; Url = "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.3.0/mysql-connector-j-8.3.0.jar" },
    @{ Name = "gson-2.10.1.jar"; Url = "https://repo1.maven.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar" },
    @{ Name = "pdfbox-2.0.30.jar"; Url = "https://repo1.maven.org/maven2/org/apache/pdfbox/pdfbox/2.0.30/pdfbox-2.0.30.jar" },
    @{ Name = "fontbox-2.0.30.jar"; Url = "https://repo1.maven.org/maven2/org/apache/pdfbox/fontbox/2.0.30/fontbox-2.0.30.jar" },
    @{ Name = "commons-logging-1.2.jar"; Url = "https://repo1.maven.org/maven2/commons-logging/commons-logging/1.2/commons-logging-1.2.jar" },
    @{ Name = "poi-5.2.5.jar"; Url = "https://repo1.maven.org/maven2/org/apache/poi/poi/5.2.5/poi-5.2.5.jar" },
    @{ Name = "poi-ooxml-5.2.5.jar"; Url = "https://repo1.maven.org/maven2/org/apache/poi/poi-ooxml/5.2.5/poi-ooxml-5.2.5.jar" },
    @{ Name = "poi-ooxml-lite-5.2.5.jar"; Url = "https://repo1.maven.org/maven2/org/apache/poi/poi-ooxml-lite/5.2.5/poi-ooxml-lite-5.2.5.jar" },
    @{ Name = "xmlbeans-5.2.0.jar"; Url = "https://repo1.maven.org/maven2/org/apache/xmlbeans/xmlbeans/5.2.0/xmlbeans-5.2.0.jar" },
    @{ Name = "commons-compress-1.26.0.jar"; Url = "https://repo1.maven.org/maven2/org/apache/commons/commons-compress/1.26.0/commons-compress-1.26.0.jar" },
    @{ Name = "commons-io-2.15.1.jar"; Url = "https://repo1.maven.org/maven2/commons-io/commons-io/2.15.1/commons-io-2.15.1.jar" },
    @{ Name = "log4j-api-2.22.1.jar"; Url = "https://repo1.maven.org/maven2/org/apache/logging/log4j/log4j-api/2.22.1/log4j-api-2.22.1.jar" }
)

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "   BlindHire - Downloading External Dependencies (JARs)   " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

foreach ($lib in $libraries) {
    $targetPath = Join-Path $libDir $lib.Name
    if (Test-Path $targetPath) {
        Write-Host " [EXISTS] $($lib.Name)" -ForegroundColor Green
    } else {
        Write-Host " [DOWNLOADING] $($lib.Name)..." -ForegroundColor Yellow
        try {
            [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
            Invoke-WebRequest -Uri $lib.Url -OutFile $targetPath -UseBasicParsing
            Write-Host " [OK] Downloaded $($lib.Name)" -ForegroundColor Green
        } catch {
            Write-Host " [ERROR] Failed to download $($lib.Name): $_" -ForegroundColor Red
        }
    }
}

Write-Host ""
Write-Host "All library downloads processed. Checking lib directory contents:" -ForegroundColor Cyan
Get-ChildItem $libDir | Select-Object Name, Length | Format-Table -AutoSize
