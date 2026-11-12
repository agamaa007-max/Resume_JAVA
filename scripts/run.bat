@echo off
setlocal

echo ==================================================================
echo         BlindHire - Launching Java HTTP Server
echo ==================================================================

:: 1. Locate java runtime binary
set "JAVA_BIN="
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_BIN=%JAVA_HOME%\bin\java.exe"
)
if "%JAVA_BIN%"=="" (
    for /d %%D in ("C:\Program Files\Java\jdk*") do (
        if exist "%%D\bin\java.exe" set "JAVA_BIN=%%D\bin\java.exe"
    )
)
if "%JAVA_BIN%"=="" (
    where java >nul 2>&1
    if %errorlevel% equ 0 set "JAVA_BIN=java"
)

if "%JAVA_BIN%"=="" (
    echo [ERROR] 'java' runtime was not found!
    echo Please install JDK 17+ and ensure JAVA_HOME is configured.
    pause
    exit /b 1
)

echo [INFO] Using Java runtime: %JAVA_BIN%

:: 2. Check if compiled classes exist
if not exist "%~dp0..\backend\bin\com\blindhire\Main.class" (
    echo [NOTICE] Compiled binaries not found. Running compilation first...
    call "%~dp0compile.bat"
    if %errorlevel% neq 0 exit /b %errorlevel%
)

:: 3. Check for Port 8080 Conflict
netstat -ano | findstr /R /C:":8080 .*LISTENING" >nul 2>&1
if %errorlevel% equ 0 (
    echo [WARNING] Port 8080 appears to be currently in use by another program!
    echo If the server fails to bind, please close the competing process or
    echo configure server.port=8081 in config.properties.
)

:: 4. Launch Application Server from project root
cd /D "%~dp0.."
echo [RUN] Starting BlindHire Application Server...
"%JAVA_BIN%" -cp "backend/bin;backend/lib/*" com.blindhire.Main

if %errorlevel% neq 0 (
    echo.
    echo [EXIT] Server stopped with code %errorlevel%.
    pause
)
