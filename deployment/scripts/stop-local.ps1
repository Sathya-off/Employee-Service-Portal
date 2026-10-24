<#
.SYNOPSIS
  Clean Shutdown Script for Local Deployment
  Finds and terminates any Node.js processes running the portal server on port 8080.
#>

$port = 8080
Write-Host "Checking for active processes listening on port $port..." -ForegroundColor Yellow

$connections = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
if ($connections) {
    foreach ($conn in $connections) {
        $procId = $conn.OwningProcess
        try {
            $proc = Get-Process -Id $procId -ErrorAction Stop
            Write-Host "Terminating process '$($proc.ProcessName)' (PID: $procId) on port $port..." -ForegroundColor Cyan
            Stop-Process -Id $procId -Force
            Write-Host " Stopped PID $procId successfully." -ForegroundColor Green
        } catch {
            Write-Host "Could not terminate PID $procId: $_" -ForegroundColor DarkGray
        }
    }
} else {
    Write-Host "No active processes found listening on port $port." -ForegroundColor Gray
}

Write-Host "Portal shutdown complete." -ForegroundColor Green
