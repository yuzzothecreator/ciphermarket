param(
    [int]$TimeoutSeconds = 300
)

$ErrorActionPreference = "Stop"

$services = @(
    @{ Name = "PostgreSQL"; Url = "http://localhost:5432"; Type = "tcp"; Host = "localhost"; Port = 5432 },
    @{ Name = "Redis"; Url = "http://localhost:6379"; Type = "tcp"; Host = "localhost"; Port = 6379 },
    @{ Name = "Keycloak"; Url = "http://localhost:8180"; Type = "http"; Host = "localhost"; Port = 8180 },
    @{ Name = "Vault"; Url = "http://localhost:8200/v1/sys/health"; Type = "http"; Host = "localhost"; Port = 8200 }
)

function Test-TcpPort {
    param([string]$HostName, [int]$Port)
    try {
        $client = New-Object System.Net.Sockets.TcpClient
        $async = $client.BeginConnect($HostName, $Port, $null, $null)
        $wait = $async.AsyncWaitHandle.WaitOne(2000, $false)
        if (-not $wait) { return $false }
        $client.EndConnect($async)
        $client.Close()
        return $true
    } catch {
        return $false
    }
}

function Test-HttpEndpoint {
    param([string]$Url)
    try {
        $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
        return $response.StatusCode -lt 500
    } catch {
        if ($_.Exception.Response) {
            return $_.Exception.Response.StatusCode.value__ -lt 500
        }
        return $false
    }
}

$deadline = (Get-Date).AddSeconds($TimeoutSeconds)

foreach ($service in $services) {
    Write-Host "Waiting for $($service.Name)..."
    while ((Get-Date) -lt $deadline) {
        $ready = if ($service.Type -eq "tcp") {
            Test-TcpPort -HostName $service.Host -Port $service.Port
        } else {
            Test-HttpEndpoint -Url $service.Url
        }
        if ($ready) {
            Write-Host "  $($service.Name) is ready."
            break
        }
        Start-Sleep -Seconds 3
    }
    if ((Get-Date) -ge $deadline) {
        Write-Error "$($service.Name) did not become ready within ${TimeoutSeconds}s"
    }
}

Write-Host "Core services are ready."
