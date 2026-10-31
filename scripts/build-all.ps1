$ErrorActionPreference = "Stop"
$services = @("ingestion-service", "hub-service", "delay-stage-service", "transit-service")
foreach ($service in $services) {
    Write-Host "== Building $service =="
    Push-Location (Join-Path $PSScriptRoot "..\$service")
    try { mvn test package; if ($LASTEXITCODE -ne 0) { throw "Build failed for $service" } }
    finally { Pop-Location }
}
