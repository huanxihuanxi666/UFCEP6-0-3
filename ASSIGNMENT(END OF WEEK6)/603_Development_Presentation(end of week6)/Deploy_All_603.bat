@echo off
setlocal
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0Deploy_All_603.ps1"
echo.
if errorlevel 1 (
  echo Deployment failed. Copy the error shown above and send it to Codex.
) else (
  echo Deployment finished. Open http://127.0.0.1:8080/operate
)
pause
