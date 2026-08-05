param(
    [string]$BaseUrl = "http://localhost:8081"
)

$ErrorActionPreference = "Stop"

$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$environmentFile = Join-Path $repositoryRoot ".env.docker.local"

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "Docker CLI was not found. Install and start Docker Desktop."
}

$dockerInfo = Start-Process `
    -FilePath "docker.exe" `
    -ArgumentList @("info") `
    -NoNewWindow `
    -Wait `
    -PassThru

if ($dockerInfo.ExitCode -ne 0) {
    throw "Docker is installed, but the Docker engine is not running."
}

if (-not (Test-Path $environmentFile)) {
    $password = ([guid]::NewGuid().ToString("N") +
                 [guid]::NewGuid().ToString("N"))

    "DOCKER_DB_PASSWORD=$password" |
        Set-Content -Path $environmentFile -Encoding ascii

    Write-Host "Created ignored local Docker environment file."
}

Push-Location $repositoryRoot

try {
    Write-Host "Validating Docker Compose configuration..."

    $configProcess = Start-Process `
        -FilePath "docker.exe" `
        -ArgumentList @(
            "compose",
            "--env-file",
            ".env.docker.local",
            "config",
            "--quiet"
        ) `
        -NoNewWindow `
        -Wait `
        -PassThru

    if ($configProcess.ExitCode -ne 0) {
        throw "Docker Compose configuration validation failed."
    }

    Write-Host "Building and starting services..."

    $composeProcess = Start-Process `
        -FilePath "docker.exe" `
        -ArgumentList @(
            "compose",
            "--env-file",
            ".env.docker.local",
            "up",
            "--build",
            "--detach"
        ) `
        -NoNewWindow `
        -Wait `
        -PassThru

    if ($composeProcess.ExitCode -ne 0) {
        Write-Host ""
        Write-Host "Container status:"
        & docker compose --env-file .env.docker.local ps --all

        Write-Host ""
        Write-Host "Recent service logs:"
        & docker compose --env-file .env.docker.local logs --tail 100

        throw "Docker Compose failed to start the services (exit code $($composeProcess.ExitCode))."
    }

    $ready = $false

    for ($attempt = 1; $attempt -le 60; $attempt++) {
        try {
            $health = Invoke-RestMethod `
                -Method Get `
                -Uri "$BaseUrl/actuator/health" `
                -TimeoutSec 2

            if ($health.status -eq "UP") {
                $ready = $true
                break
            }
        }
        catch {
            Start-Sleep -Seconds 2
        }
    }

    if (-not $ready) {
        & docker compose `
            --env-file .env.docker.local `
            ps `
            --all

        & docker compose `
            --env-file .env.docker.local `
            logs `
            app `
            --tail 100

        throw "The application did not become healthy within two minutes."
    }

    Write-Host ""
    Write-Host "Docker environment is ready."
    Write-Host "Health:  $BaseUrl/actuator/health"
    Write-Host "Swagger: $BaseUrl/swagger-ui.html"
    Write-Host ""

    & docker compose --env-file .env.docker.local ps
}
finally {
    Pop-Location
}
