@echo off
setlocal
echo ==========================================================
echo    BlindHire - Downloading External Dependencies (JARs)
echo ==========================================================
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0download-libs.ps1"
echo Done.
pause
