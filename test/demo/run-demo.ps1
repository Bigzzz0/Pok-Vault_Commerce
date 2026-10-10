param(
    [ValidateSet('manual', 'timed', 'fast')][string]$Mode = 'manual',
    [ValidateRange(0.1, 10.0)][double]$Speed = 1.5,
    [string]$BaseUrl = 'https://pok-vault-commerce.onrender.com',
    [string]$CardName = '',
    [string]$Customer = 'customer_red',
    [string]$Seller = 'staff_ash',
    [switch]$Setup,
    [switch]$DryRun,
    [switch]$RecordVideo
)
$ErrorActionPreference = 'Stop'
$demoDir = $PSScriptRoot
$demoPython = Join-Path $demoDir '.venv/Scripts/python.exe'
$demoBrowser = Join-Path $demoDir '.venv/Scripts/rfbrowser.exe'
# Prefer the machine's Node/Python; use Codex's bundled runtime when available.
$runtimeDir = Join-Path $env:USERPROFILE '.cache/codex-runtimes/codex-primary-runtime/dependencies'
$nodeDir = Join-Path $runtimeDir 'node/bin'
if (Test-Path -LiteralPath (Join-Path $nodeDir 'node.exe')) {
    $env:Path = $nodeDir + ';' + $env:Path
}
if ($Setup) {
    if (-not (Test-Path -LiteralPath $demoPython)) {
        $bootstrapPython = Join-Path $runtimeDir 'python/python.exe'
        if (-not (Test-Path -LiteralPath $bootstrapPython)) { $bootstrapPython = 'python' }
        & $bootstrapPython -m venv (Join-Path $demoDir '.venv')
        if ($LASTEXITCODE -ne 0) { throw 'Creating the Python environment failed.' }
    }
    & $demoPython -m pip install -r (Join-Path $demoDir 'requirements.txt')
    if ($LASTEXITCODE -ne 0) { throw 'Installing Robot dependencies failed.' }
    & $demoBrowser init chromium
    if ($LASTEXITCODE -ne 0) { throw 'Installing Browser dependencies failed.' }
    Write-Host 'Setup finished. Run this script again without -Setup to start the demo.'
    exit 0
}
if (-not (Test-Path -LiteralPath $demoPython)) { throw 'Run with -Setup first.' }
if (-not $env:POKEV_DEMO_CUSTOMER_PASSWORD) { $env:POKEV_DEMO_CUSTOMER_PASSWORD = 'password123' }
if (-not $env:POKEV_DEMO_SELLER_PASSWORD) { $env:POKEV_DEMO_SELLER_PASSWORD = 'password123' }
$runStamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$outputDir = Join-Path $demoDir "output/$runStamp"
$robotArgs = @('-X', 'utf8', '-m', 'robot', '--outputdir', $outputDir,
    '--variable', "BASE_URL:$($BaseUrl.TrimEnd('/'))", '--variable', "MODE:$Mode",
    '--variable', "SPEED:$($Speed.ToString([System.Globalization.CultureInfo]::InvariantCulture))",
    '--variable', "CARD_NAME:$CardName", '--variable', "CUSTOMER:$Customer",
    '--variable', "SELLER:$Seller", '--variable', "RECORD_VIDEO:$($RecordVideo.IsPresent)")
if ($DryRun) { $robotArgs += '--dryrun' }
$robotArgs += (Join-Path $demoDir 'two-perspectives.robot')
if (-not $DryRun) {
    Write-Host "Target: $BaseUrl | Mode: $Mode"
    Write-Host 'This demo creates ONE real order and consumes stock. It records simulated payment/trade completion.'
    if ($Mode -eq 'manual') {
        Write-Host 'Click the small Continue button in the browser at each speech checkpoint.'
    } elseif ($Mode -eq 'timed') {
        Write-Host 'Timed mode: the browser continues automatically at each speech checkpoint.'
    }
}
& $demoPython @robotArgs
$demoExit = $LASTEXITCODE
Write-Host "Output: $outputDir"
exit $demoExit
