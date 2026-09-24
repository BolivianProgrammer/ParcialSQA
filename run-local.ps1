$ErrorActionPreference = 'Stop'
Set-Location -LiteralPath $PSScriptRoot

# Read the local database password without publishing it in Git.
$envFile = Join-Path $PSScriptRoot '.env'
if (Test-Path -LiteralPath $envFile) {
    foreach ($line in Get-Content -LiteralPath $envFile) {
        if ($line -match '^MYSQL_ROOT_PASSWORD=(.+)$') {
            $env:MYSQL_ROOT_PASSWORD = $Matches[1]
        }
    }
}
if ([string]::IsNullOrWhiteSpace($env:MYSQL_ROOT_PASSWORD)) {
    throw 'Set MYSQL_ROOT_PASSWORD in a local .env file before starting the application.'
}

# Use the side-by-side Java 17 install for this course project.
$jdkRoot = Join-Path $env:LOCALAPPDATA 'Programs\Eclipse Adoptium'
$jdk = Get-ChildItem -LiteralPath $jdkRoot -Directory -Filter 'jdk-17*' |
    Sort-Object Name -Descending | Select-Object -First 1
if (-not $jdk) { throw 'Install a Java 17 JDK under LocalAppData\Programs\Eclipse Adoptium first.' }
$env:JAVA_HOME = $jdk.FullName
$dockerBin = Join-Path $env:LOCALAPPDATA 'Programs\DockerDesktop\resources\bin'
$env:PATH = "$env:JAVA_HOME\bin;$dockerBin;$env:PATH"

if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) {
    throw 'Port 8080 is already in use. The application may already be running.'
}
docker compose up -d --wait --wait-timeout 120
if ($LASTEXITCODE -ne 0) { throw 'MySQL did not become healthy. Check Docker Desktop and docker compose logs.' }

& .\mvnw.cmd -B -ntp spring-boot:run '-Dspring-boot.run.arguments=--server.address=127.0.0.1'
exit $LASTEXITCODE
