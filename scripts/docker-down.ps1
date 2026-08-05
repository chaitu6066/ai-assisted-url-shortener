param(
    [switch]$RemoveData
)

$ErrorActionPreference = "Stop"

$repositoryRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$environmentFile = Join-Path $repositoryRoot ".env.docker.local"

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "Docker CLI was not found."
}

Push-Location $repositoryRoot

try {
    if (Test-Path $environmentFile) {
        $composePrefix = @(
            "compose",
            "--env-file",
            ".env.docker.local"
        )
    }
    else {
        $env:DOCKER_DB_PASSWORD = "unused-for-shutdown"
        $composePrefix = @("compose")
    }

    $downArguments = @(
        "down",
        "--remove-orphans"
    )

    if ($RemoveData) {
        $downArguments += "--volumes"
    }

    & docker @composePrefix @downArguments

    if ($LASTEXITCODE -ne 0) {
        throw "Docker Compose failed to stop the services."
    }

    if ($RemoveData) {
        Write-Host "Containers, network, and PostgreSQL volume removed."
    }
    else {
        Write-Host "Containers and network removed. PostgreSQL data retained."
    }
}
finally {
    Pop-Location
}
