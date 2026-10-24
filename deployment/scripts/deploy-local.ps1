<#
.SYNOPSIS
  Automated Local Deployment & Verification Script
  Starts the Employee Portal Application, verifies readiness, and triggers regression verification.
#>

param(
    [ValidateSet("smoke", "regression")]
    [string]$TestSuite = "smoke",
    [switch]$Headed = $false
)

$ErrorActionPreference = "Stop"

$projectRoot = Resolve-Path "$PSScriptRoot\..\.."
$appDir = "$projectRoot\employee-portal-app\backend"
$testDir = "$projectRoot\automation-testing-framework"
$port = 8080
$healthUrl = "http://localhost:$port/api/health"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Enterprise Portal Local Deployment & Verification" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# Step 1: Check if port 8080 is already active
Write-Host "`n[1/4] Checking Application Port $port..." -ForegroundColor Yellow
$isRunning = $false
try {
    $res = Invoke-RestMethod -Uri $healthUrl -Method Get -TimeoutSec 2 -ErrorAction SilentlyContinue
    if ($res.status -eq "UP") {
        Write-Host "  Application is ALREADY running and healthy on port $port." -ForegroundColor Green
        $isRunning = $true
    }
} catch {
    $isRunning = $false
}

if (-not $isRunning) {
    Write-Host "  Starting Employee Portal application server..." -ForegroundColor Yellow
    $serverScript = "$appDir\server.js"
    $process = Start-Process -FilePath "node" -ArgumentList "`"$serverScript`"" -WorkingDirectory $appDir -PassThru -WindowStyle Hidden
    Write-Host "  Spawned Node.js process with PID: $($process.Id)" -ForegroundColor Gray

    # Step 2: Poll Healthcheck endpoint
    Write-Host "`n[2/4] Waiting for Application Healthcheck Readiness..." -ForegroundColor Yellow
    $ready = $false
    $retries = 15
    while ($retries -gt 0 -and -not $ready) {
        Start-Sleep -Seconds 1
        try {
            $health = Invoke-RestMethod -Uri $healthUrl -Method Get -TimeoutSec 2 -ErrorAction SilentlyContinue
            if ($health.status -eq "UP") {
                $ready = $true
                Write-Host "   Healthcheck verified! SUT is UP and accepting traffic." -ForegroundColor Green
                break
            }
        } catch {
            Write-Host "  Polling healthcheck... ($retries retries remaining)" -ForegroundColor DarkGray
        }
        $retries--
    }

    if (-not $ready) {
        Write-Host "❌ Application failed to become healthy within timeout period." -ForegroundColor Red
        exit 1
    }
}

# Step 3: Configure and Launch Automated Verification Suite
Write-Host "`n[3/4] Launching Automated Testing Suite ($TestSuite)..." -ForegroundColor Yellow

$suiteFile = if ($TestSuite -eq "smoke") { "testng-smoke.xml" } else { "testng.xml" }
$headlessParam = if ($Headed) { "-Dheadless=false" } else { "-Dheadless=true" }

$mvnExe = "C:\Users\MC HP\AppData\Local\Programs\apache-maven-3.9.9\bin\mvn.cmd"
if (-not (Test-Path $mvnExe)) {
    $mvnExe = "mvn"
}

$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
$env:Path = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin;C:\Users\MC HP\AppData\Local\Programs\apache-maven-3.9.9\bin;" + $env:Path

Push-Location $testDir
try {
    $suiteArg = "-DsuiteXmlFile=$suiteFile"
    Write-Host "  Executing: $mvnExe test $suiteArg $headlessParam" -ForegroundColor Cyan
    & $mvnExe test $suiteArg $headlessParam
    $testExitCode = $LASTEXITCODE

    Write-Host "`n[4/4] Deployment & Testing Summary:" -ForegroundColor Yellow
    if ($testExitCode -eq 0) {
        Write-Host "==========================================================" -ForegroundColor Green
        Write-Host " DEPLOYMENT & VERIFICATION SUCCESSFUL!" -ForegroundColor Green
        Write-Host " Portal URL: http://localhost:$port" -ForegroundColor Green
        Write-Host " Report: file://$testDir/target/surefire-reports/index.html" -ForegroundColor Green
        Write-Host "==========================================================" -ForegroundColor Green
    } else {
        Write-Host "==========================================================" -ForegroundColor Red
        Write-Host "❌ DEPLOYMENT FAILED VERIFICATION (Test Failures Detected)" -ForegroundColor Red
        Write-Host " Review reports in: $testDir\target\surefire-reports" -ForegroundColor Red
        Write-Host "==========================================================" -ForegroundColor Red
        exit $testExitCode
    }
} finally {
    Pop-Location
}
