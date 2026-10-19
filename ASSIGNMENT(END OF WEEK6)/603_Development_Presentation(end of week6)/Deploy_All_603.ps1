# One-click deployer for the Release 2 BPMN processes and Camunda forms.
# Run: right-click > Run with PowerShell, or execute .\Deploy_All_603.ps1
$ErrorActionPreference = 'Stop'

$root = 'D:\Camunda-Workspace\models\ASSIGNMENT(END OF WEEK6)\603_Development_Presentation(end of week6)'
$bpmnFolder = Join-Path $root '02_Release2_Integrated_BPMN'
$formFolder = Join-Path $root '03_Camunda_Forms'
$endpoint = 'http://127.0.0.1:8080/v2/deployments'
$c8run = 'D:\camunda8-getting-started-bundle-8.10.0-alpha5-windows-x86_641\c8run-8.10.0-alpha5\c8run.exe'

function Test-CamundaReady {
  return [bool](Test-NetConnection -ComputerName '127.0.0.1' -Port 8080 -InformationLevel Quiet -WarningAction SilentlyContinue)
}

if (-not (Test-Path -LiteralPath $bpmnFolder)) { throw "BPMN folder not found: $bpmnFolder" }
if (-not (Test-Path -LiteralPath $formFolder)) { throw "Forms folder not found: $formFolder" }

if (-not (Test-CamundaReady)) {
  if (-not (Test-Path -LiteralPath $c8run)) { throw "Camunda is not ready and c8run was not found: $c8run" }
  Write-Host 'Starting local Camunda 8 Run...' -ForegroundColor Yellow
  Start-Process -FilePath $c8run -ArgumentList 'start' -WorkingDirectory (Split-Path $c8run) -WindowStyle Hidden
  $deadline = (Get-Date).AddSeconds(55)
  do {
    Start-Sleep -Seconds 2
    if (Test-CamundaReady) { break }
  } while ((Get-Date) -lt $deadline)
  if (-not (Test-CamundaReady)) { throw 'Camunda did not become ready on http://127.0.0.1:8080 within 55 seconds.' }
}

$bpmn = @(Get-ChildItem -LiteralPath $bpmnFolder -Recurse -File -Filter '*.bpmn' | Sort-Object FullName)
$forms = @(Get-ChildItem -LiteralPath $formFolder -Recurse -File -Filter '*.form' | Sort-Object FullName)
if ($bpmn.Count -eq 0) { throw 'No BPMN files found.' }
if ($forms.Count -eq 0) { throw 'No Camunda form files found.' }

# curl.exe is included with current Windows versions and works in both Windows PowerShell 5 and PowerShell 7.
$curlArgs = @('-sS', '--fail-with-body', '-X', 'POST', $endpoint)
foreach ($file in @($bpmn + $forms)) {
  $contentType = if ($file.Extension -eq '.bpmn') { 'application/xml' } else { 'application/json' }
  $curlArgs += '-F'
  $curlArgs += ('resources=@"{0}";type={1}' -f $file.FullName, $contentType)
}
$body = & curl.exe @curlArgs 2>&1
if ($LASTEXITCODE -ne 0) { throw "Deployment failed (curl exit code $LASTEXITCODE):`n$body" }

Write-Host "DEPLOYED SUCCESSFULLY: $($bpmn.Count) BPMN + $($forms.Count) forms" -ForegroundColor Green
Write-Host 'Open: http://127.0.0.1:8080/operate' -ForegroundColor Cyan
Write-Host $body
