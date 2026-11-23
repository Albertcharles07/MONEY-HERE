[CmdletBinding()]
param(
    [ValidateSet('Start', 'VerifyNeon')]
    [string]$Mode = 'Start',
    [string]$EnvFile,
    [switch]$DefaultAdmin
)

$ErrorActionPreference = 'Stop'

function Test-LocalPort([string]$Address, [int]$Port) {
    $client = [Net.Sockets.TcpClient]::new()
    try {
        $connection = $client.ConnectAsync($Address, $Port)
        return $connection.Wait(800) -and $client.Connected
    } catch {
        return $false
    } finally {
        $client.Dispose()
    }
}

function Show-MoneyHereMobile {
    $addresses = [Net.NetworkInformation.NetworkInterface]::GetAllNetworkInterfaces() |
        Where-Object { $_.OperationalStatus -eq 'Up' -and $_.NetworkInterfaceType -in @('Wireless80211', 'Ethernet') } |
        ForEach-Object {
            $ip = $_.GetIPProperties()
            if ($ip.GatewayAddresses | Where-Object { $_.Address.AddressFamily -eq 'InterNetwork' -and $_.Address.ToString() -ne '0.0.0.0' }) {
                $ip.UnicastAddresses | Where-Object { $_.Address.AddressFamily -eq 'InterNetwork' } |
                    ForEach-Object { $_.Address.ToString() }
            }
        } | Select-Object -Unique
    foreach ($address in $addresses) {
        Write-Host "Expo Go (SDK 54) no mesmo Wi-Fi: exp://${address}:8081"
    }
}
function Start-MoneyHereWeb {
    $url = 'http://localhost:8081'
    if (Test-LocalPort 'localhost' 8081) {
        $status = Invoke-RestMethod -UseBasicParsing -Uri "$url/status" -TimeoutSec 5
        if ($status -ne 'packager-status:running') {
            throw 'A porta 8081 está em uso por outro serviço.'
        }
        Write-Host "Expo já ativo. Abrindo o site em $url."
        Show-MoneyHereMobile
        Start-Process $url
        return
    }

    $frontend = Join-Path (Split-Path -Parent $PSScriptRoot) 'frontend'
    $cli = Join-Path $frontend 'node_modules/expo/bin/cli'
    if (-not (Test-Path -LiteralPath $cli)) {
        throw 'Instale as dependências com npm.cmd install na pasta frontend e execute este comando novamente.'
    }
    $node = (Get-Command node.exe -CommandType Application -ErrorAction Stop).Source
    $logs = Join-Path $frontend '.expo'
    New-Item -ItemType Directory -Path $logs -Force | Out-Null
    Start-Process -FilePath $node -WorkingDirectory $frontend -WindowStyle Hidden `
        -ArgumentList @('node_modules/expo/bin/cli', 'start', '--lan', '--web', '--port', '8081') `
        -RedirectStandardOutput (Join-Path $logs 'money-here-start.log') `
        -RedirectStandardError (Join-Path $logs 'money-here-start-error.log')
    Write-Host "Iniciando o Expo. O site abrirá em $url quando estiver pronto."
    Show-MoneyHereMobile
    Write-Host 'Logs do Expo: frontend/.expo/money-here-start.log e money-here-start-error.log.'
}

if ($Mode -eq 'Start') {
    $port = 8080
    if ($env:PORT -and (-not [int]::TryParse($env:PORT, [ref]$port) -or $port -lt 1 -or $port -gt 65535)) {
        throw 'PORT precisa ser um número entre 1 e 65535.'
    }

    $address = 'localhost'
    if ($env:SERVER_ADDRESS -and $env:SERVER_ADDRESS -notin @('0.0.0.0', '::')) {
        $address = $env:SERVER_ADDRESS
    }
    if (Test-LocalPort $address $port) {
        $base = [UriBuilder]::new('http', $address, $port).Uri.AbsoluteUri.TrimEnd('/')
        $existing = $false
        try {
            $health = Invoke-RestMethod -UseBasicParsing -Uri "$base/actuator/health" -TimeoutSec 4
            $article = Invoke-RestMethod -UseBasicParsing -Uri "$base/api/public/content/orcamento-50-30-20" -TimeoutSec 4
            $existing = $health.status -eq 'UP' -and $article.key -eq 'orcamento-50-30-20'
        } catch {}
        if (-not $existing) {
            throw "A porta $port está ocupada. Aguarde se o backend estiver iniciando ou confira qual serviço está usando a porta."
        }
        Write-Host "MONEY HERE já está rodando em $base. Use a instância atual."
        if ($DefaultAdmin -or $PSBoundParameters.ContainsKey('EnvFile')) {
            Write-Host 'As opções de inicialização só são aplicadas ao iniciar uma nova instância.'
        }
        if ($DefaultAdmin) { Start-MoneyHereWeb }
        return
    }
}

if (-not $EnvFile) {
    $EnvFile = Join-Path (Split-Path -Parent $PSScriptRoot) '.env.local'
}

function Convert-NeonConnection([string]$Value, [string]$Name) {
    $uri = $null
    if (-not [Uri]::TryCreate($Value, [UriKind]::Absolute, [ref]$uri) -or
        $uri.Scheme -notin @('postgres', 'postgresql') -or
        $uri.Host -notmatch '\.neon\.tech$' -or $uri.AbsolutePath.Length -le 1 -or
        $uri.Fragment) {
        throw "$Name precisa conter uma URL PostgreSQL válida do Neon."
    }

    $credentials = $uri.UserInfo -split ':', 2
    if ($credentials.Count -ne 2 -or -not $credentials[0] -or -not $credentials[1]) {
        throw "$Name precisa conter usuário e senha."
    }

    $options = @($uri.Query.TrimStart('?') -split '&' | Where-Object {
        $_ -match '^(channel_binding|channelBinding)=(require|prefer|disable)$'
    } | ForEach-Object { $_ -replace '^channel_binding=', 'channelBinding=' })
    $options += 'sslmode=verify-full', 'sslfactory=org.postgresql.ssl.DefaultJavaSSLFactory'
    $port = if ($uri.IsDefaultPort) { 5432 } else { $uri.Port }
    @{
        Url = 'jdbc:postgresql://{0}:{1}{2}?{3}' -f $uri.Host, $port, $uri.AbsolutePath, ($options -join '&')
        Host = $uri.Host
        Database = $uri.AbsolutePath
        User = [Uri]::UnescapeDataString($credentials[0])
        Password = [Uri]::UnescapeDataString($credentials[1])
    }
}

$mailSettings = @('MAIL_HOST', 'MAIL_PORT', 'MAIL_USERNAME', 'MAIL_PASSWORD', 'MAIL_FROM',
    'MAIL_AUTH', 'MAIL_TLS', 'MAIL_HEALTH_ENABLED', 'PUBLIC_URL')
$values = @{}
foreach ($line in [IO.File]::ReadAllLines((Resolve-Path -LiteralPath $EnvFile).Path)) {
    if ($line -match '^\s*(?:export\s+)?([A-Z_]+)\s*=\s*(.*?)\s*$') {
        $key, $value = $Matches[1], $Matches[2]
        if ($key -notin $mailSettings -and $key -notin @('DATABASE_URL', 'DATABASE_URL_UNPOOLED', 'JWT_SECRET')) { continue }
        if ($value -match '^"(.*)"$' -or $value -match "^'(.*)'$") {
            $value = $Matches[1]
        }
        $values[$key] = $value
    }
}

$direct = Convert-NeonConnection $values['DATABASE_URL_UNPOOLED'] 'DATABASE_URL_UNPOOLED'
$application = Convert-NeonConnection $values['DATABASE_URL'] 'DATABASE_URL'
if ($direct.Host -match '-pooler\.' -or
    ($application.Host -replace '-pooler\.', '.') -ne $direct.Host -or
    $direct.Database -cne $application.Database) {
    throw 'As duas URLs precisam apontar para o mesmo banco e a URL de migração deve ser direta.'
}

$jwt = $env:JWT_SECRET
if (-not $jwt) { $jwt = $values['JWT_SECRET'] }
if (-not $jwt) {
    $bytes = New-Object byte[] 32
    $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
    $jwt = [Convert]::ToBase64String($bytes)
}
if ([Text.Encoding]::UTF8.GetByteCount($jwt) -lt 32) {
    throw 'JWT_SECRET precisa ter pelo menos 32 bytes.'
}

$wrapper = Join-Path $PSScriptRoot 'mvnw.cmd'
$maven = if (Test-Path -LiteralPath $wrapper) { $wrapper } else {
    $command = Get-Command mvn.cmd -CommandType Application -ErrorAction SilentlyContinue
    if ($command) {
        $command.Source
    } else {
        $cache = Join-Path $env:USERPROFILE '.m2\wrapper\dists'
        $distributions = Get-ChildItem -LiteralPath $cache -Directory -ErrorAction SilentlyContinue |
            Where-Object { $_.Name -match '^apache-maven-\d+\.\d+\.\d+$' } |
            Sort-Object { [version]($_.Name -replace '^apache-maven-', '') } -Descending
        $cachedMaven = $distributions | ForEach-Object {
            Get-ChildItem -LiteralPath $_.FullName -Directory -ErrorAction SilentlyContinue
        } | ForEach-Object {
            Get-Item -LiteralPath (Join-Path $_.FullName 'bin\mvn.cmd') -ErrorAction SilentlyContinue
        } | Select-Object -First 1
        if (-not $cachedMaven) {
            throw 'Maven não encontrado. Instale o Maven 3.9+ e adicione sua pasta bin ao PATH.'
        }
        $cachedMaven.FullName
    }
}

$settings = @{
    SPRING_DATASOURCE_URL = $application.Url
    SPRING_DATASOURCE_USERNAME = $application.User
    SPRING_DATASOURCE_PASSWORD = $application.Password
    SPRING_FLYWAY_URL = $direct.Url
    SPRING_FLYWAY_USER = $direct.User
    SPRING_FLYWAY_PASSWORD = $direct.Password
    JWT_SECRET = $jwt
    MONEY_HERE_LOCAL_LAN = 'true'
}
foreach ($key in $mailSettings) {
    if ($values.ContainsKey($key) -and -not [Environment]::GetEnvironmentVariable($key)) {
        $settings[$key] = $values[$key]
    }
}
if ($DefaultAdmin) {
    if ($Mode -ne 'Start') { throw 'Use -DefaultAdmin somente ao iniciar o backend.' }
    $settings['MONEY_HERE_ADMIN_EMAIL'] = 'admin@moneyhere.local'
    $settings['MONEY_HERE_ADMIN_PASSWORD'] = 'admin@123'
    $settings['MONEY_HERE_ADMIN_TEMPORARY'] = 'true'
    # A senha inicial continua limitada a requisições locais pela API.
}
if ($Mode -eq 'VerifyNeon') { $settings['MONEY_HERE_NEON_TEST'] = 'true' }
if ($DefaultAdmin) {
    Start-MoneyHereWeb
    Write-Host "Iniciando a API na porta $port. Aguarde Started MoneyHereApplication antes de entrar."
}
$previous = @{}
Push-Location -LiteralPath $PSScriptRoot
try {
    foreach ($key in $settings.Keys) {
        $previous[$key] = [Environment]::GetEnvironmentVariable($key)
        [Environment]::SetEnvironmentVariable($key, $settings[$key])
    }
    if ($Mode -eq 'VerifyNeon') {
        & $maven clean verify
    } else {
        & $maven spring-boot:run
    }
    if ($LASTEXITCODE -ne 0) { throw "Maven terminou com código $LASTEXITCODE." }
} finally {
    foreach ($key in $previous.Keys) {
        [Environment]::SetEnvironmentVariable($key, $previous[$key])
    }
    Pop-Location
}
