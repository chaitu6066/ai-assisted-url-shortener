param(
    [string]$BaseUrl = "http://localhost:8081"
)

$ErrorActionPreference = "Stop"
$alias = "smoke-" + [DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$destination = "https://www.example.com/smoke"

Write-Host "Creating custom alias: $alias"

$createBody = @{
    originalUrl = $destination
    customAlias = $alias
} | ConvertTo-Json

$created = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/v1/urls" -ContentType "application/json" -Body $createBody

if ($created.shortCode -ne $alias) {
    throw "Create assertion failed."
}

$before = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/urls/$alias/analytics"

if ([int64]$before.clickCount -ne 0) {
    throw "Expected initial clickCount 0."
}

1..3 | ForEach-Object {
    $status = curl.exe -s -o NUL --max-redirs 0 -w "%{http_code}" "$BaseUrl/$alias"

    if ($status -ne "302") {
        throw "Expected redirect 302, received $status."
    }
}

$after = Invoke-RestMethod -Method Get -Uri "$BaseUrl/api/v1/urls/$alias/analytics"

if ([int64]$after.clickCount -ne 3) {
    throw "Expected clickCount 3; received $($after.clickCount)."
}

if (-not $after.lastAccessedAt) {
    throw "Expected lastAccessedAt to be populated."
}

Write-Host "Smoke test passed."
Write-Host "Short code: $alias"
Write-Host "Click count: $($after.clickCount)"
Write-Host "Last accessed: $($after.lastAccessedAt)"
