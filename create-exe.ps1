<#
.SYNOPSIS
    Packages StudentAttendance into a standalone Windows .exe using jpackage.

.DESCRIPTION
    Automated packaging script for Student Attendance System.
    Author: Hani Al-khateeb
    Copyright (c) 2026 Hani Al-khateeb. All Rights Reserved.
#>

[CmdletBinding()]
param(
    [switch]$Installer
)

$ErrorActionPreference = "Stop"
$ScriptDir = $PSScriptRoot
if (-not $ScriptDir) { $ScriptDir = Get-Location }

Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host "    STUDENT ATTENDANCE SYSTEM  -  STANDALONE EXE PACKAGING AUTOMATION" -ForegroundColor Cyan
Write-Host "    Developer : Hani Al-khateeb | Copyright (c) 2026 All Rights Reserved" -ForegroundColor Yellow
Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host ""

# 1. Locate jpackage
Write-Host "[*] Locating JDK packaging tools (jpackage)..." -ForegroundColor Cyan
$jpackagePath = $null
$cmd = Get-Command "jpackage.exe" -ErrorAction SilentlyContinue
if ($cmd) {
    $jpackagePath = $cmd.Source
} elseif ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\jpackage.exe")) {
    $jpackagePath = "$env:JAVA_HOME\bin\jpackage.exe"
} else {
    $candidates = @(
        "C:\Program Files\Eclipse Adoptium\jdk-21*\bin\jpackage.exe",
        "C:\Program Files\Java\jdk-21*\bin\jpackage.exe",
        "C:\Program Files\Java\jdk-17*\bin\jpackage.exe",
        "C:\Program Files\Eclipse Adoptium\jdk-17*\bin\jpackage.exe"
    )
    foreach ($c in $candidates) {
        $found = Get-Item $c -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($found) {
            $jpackagePath = $found.FullName
            break
        }
    }
}

if (-not $jpackagePath) {
    Write-Error "jpackage.exe was not found. Please install JDK 17+ and add it to PATH."
    exit 1
}
Write-Host "    - jpackage found at: $jpackagePath" -ForegroundColor Green

# 2. Locate Icon
$iconPath = Join-Path $ScriptDir "src\main\resources\com\example\studentattendance\images\app_icon.ico"
if (-not (Test-Path $iconPath)) {
    $iconPath = Join-Path $ScriptDir "docs\app_icon.ico"
}
if (Test-Path $iconPath) {
    Write-Host "[*] Application icon located: $iconPath" -ForegroundColor Green
} else {
    Write-Host "[WARNING] Application icon not found. Default launcher icon will be used." -ForegroundColor Yellow
    $iconPath = $null
}

# 3. Build project
Write-Host ""
Write-Host "[*] Step 1/4: Building application package with Maven Wrapper..." -ForegroundColor Cyan
& "$ScriptDir\mvnw.cmd" clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Error "Maven compilation failed."
    exit $LASTEXITCODE
}

$mainJar = Join-Path $ScriptDir "target\StudentAttendance-1.0-SNAPSHOT.jar"
if (-not (Test-Path $mainJar)) {
    Write-Error "Main JAR $mainJar was not created."
    exit 1
}

# 4. Copy runtime dependencies
Write-Host ""
Write-Host "[*] Step 2/4: Collecting runtime dependencies..." -ForegroundColor Cyan
& "$ScriptDir\mvnw.cmd" dependency:copy-dependencies -DoutputDirectory="$ScriptDir\target\libs" -DincludeScope=runtime
if ($LASTEXITCODE -ne 0) {
    Write-Error "Failed to copy dependencies."
    exit $LASTEXITCODE
}

# 5. Staging
Write-Host ""
Write-Host "[*] Step 3/4: Assembling packaging staging environment..." -ForegroundColor Cyan
$stagingDir = Join-Path $ScriptDir "target\jpackage-staging"
if (Test-Path $stagingDir) {
    Remove-Item -Recurse -Force $stagingDir
}
New-Item -ItemType Directory -Force -Path $stagingDir | Out-Null

Copy-Item $mainJar -Destination $stagingDir
Copy-Item (Join-Path $ScriptDir "target\libs\*.jar") -Destination $stagingDir
Get-ChildItem -Path $stagingDir -Filter "*junit*" | Remove-Item -Force -ErrorAction SilentlyContinue
Get-ChildItem -Path $stagingDir -Filter "*opentest4j*" | Remove-Item -Force -ErrorAction SilentlyContinue
Get-ChildItem -Path $stagingDir -Filter "*apiguardian*" | Remove-Item -Force -ErrorAction SilentlyContinue

# 6. Determine Packaging Type
$pkgType = "app-image"
$hasWix = (Get-Command "candle.exe" -ErrorAction SilentlyContinue) -ne $null
$extraArgs = @()

if ($Installer) {
    if ($hasWix) {
        $pkgType = "exe"
        $extraArgs += @("--win-shortcut", "--win-menu", "--win-dir-chooser")
        Write-Host "[*] WiX Toolset detected. Packaging as Windows Installer (.exe)..." -ForegroundColor Green
    } else {
        Write-Host "[!] WiX Toolset not found on PATH. Falling back to standalone app-image (.exe)." -ForegroundColor Yellow
    }
}

# 7. Execute jpackage
$destDir = Join-Path $ScriptDir "dist"
$targetDir = Join-Path $destDir "StudentAttendance"
if (Test-Path $targetDir) {
    Remove-Item -Recurse -Force $targetDir
}

Write-Host ""
Write-Host "[*] Step 4/4: Running jpackage to create standalone executable..." -ForegroundColor Cyan
Write-Host "    - Application : StudentAttendance"
Write-Host "    - Version     : 1.0.0"
Write-Host "    - Vendor      : Hani Al-khateeb"
Write-Host "    - Copyright   : Copyright (c) 2026 Hani Al-khateeb. All Rights Reserved."
Write-Host "    - Output Dir  : $destDir"
Write-Host "    - Type        : $pkgType"
Write-Host ""

$jpArgs = @(
    "--type", $pkgType,
    "--name", "StudentAttendance",
    "--app-version", "1.0.0",
    "--vendor", "Hani Al-khateeb",
    "--copyright", "Copyright (c) 2026 Hani Al-khateeb. All Rights Reserved.",
    "--description", "Student Attendance System",
    "--dest", $destDir,
    "--input", $stagingDir,
    "--main-jar", "StudentAttendance-1.0-SNAPSHOT.jar",
    "--main-class", "com.example.studentattendance.Main",
    "--add-modules", "java.base,java.desktop,java.xml,java.prefs,java.management,jdk.unsupported"
)

if ($iconPath) {
    $jpArgs += @("--icon", $iconPath)
}
$jpArgs += $extraArgs

& "$jpackagePath" @jpArgs
if ($LASTEXITCODE -ne 0) {
    Write-Error "jpackage failed with exit code $LASTEXITCODE"
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "===============================================================================" -ForegroundColor Green
Write-Host "[SUCCESS] Standalone Windows executable packaged successfully!" -ForegroundColor Green
Write-Host "===============================================================================" -ForegroundColor Green
Write-Host ""
Write-Host "Executable location:" -ForegroundColor Yellow
Write-Host "  $targetDir\StudentAttendance.exe" -ForegroundColor White
Write-Host ""
Write-Host "Packaging summary:" -ForegroundColor Cyan
Write-Host "  - Executable    : StudentAttendance.exe (Self-contained, bundled slim JRE)"
Write-Host "  - Vendor        : Hani Al-khateeb"
Write-Host "  - Copyright     : Copyright (c) 2026 Hani Al-khateeb. All Rights Reserved."
Write-Host "  - Custom Icon   : Embedded in .exe binary and runtime window"
Write-Host "  - Target System : Any 64-bit Windows PC (NO Java pre-installation required)"
Write-Host ""
Write-Host "To run the application, double-click:" -ForegroundColor Yellow
Write-Host "  $targetDir\StudentAttendance.exe" -ForegroundColor White
Write-Host "===============================================================================" -ForegroundColor Green
