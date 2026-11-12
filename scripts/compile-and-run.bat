@echo off
setlocal

echo ==================================================================
echo       BlindHire - Complete Build and Server Startup
echo ==================================================================

call "%~dp0compile.bat"
if %errorlevel% neq 0 (
    echo [ERROR] Build step failed. Server startup aborted.
    pause
    exit /b %errorlevel%
)

echo.
call "%~dp0run.bat"
