@echo off
setlocal

echo ==================================================================
echo         BlindHire - Java Backend Compilation Process
echo ==================================================================

:: 1. Locate javac compiler binary
set "JAVAC_BIN="
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\javac.exe" set "JAVAC_BIN=%JAVA_HOME%\bin\javac.exe"
)
if "%JAVAC_BIN%"=="" (
    for /d %%D in ("C:\Program Files\Java\jdk*") do (
        if exist "%%D\bin\javac.exe" set "JAVAC_BIN=%%D\bin\javac.exe"
    )
)
if "%JAVAC_BIN%"=="" (
    where javac >nul 2>&1
    if %errorlevel% equ 0 set "JAVAC_BIN=javac"
)

if "%JAVAC_BIN%"=="" (
    echo [ERROR] 'javac' compiler was not found!
    echo Please ensure JDK 17+ is installed and JAVA_HOME is configured.
    pause
    exit /b 1
)

echo [INFO] Using compiler: %JAVAC_BIN%

:: 2. Ensure target bin directory exists
if not exist "%~dp0..\backend\bin" (
    mkdir "%~dp0..\backend\bin"
)

:: 3. Check for external JAR libraries
if not exist "%~dp0..\backend\lib\mysql-connector-j-8.3.0.jar" (
    echo [WARNING] External JARs not detected in backend\lib\
    echo Running download-libs.bat to obtain dependencies...
    call "%~dp0download-libs.bat"
)

:: 4. Collect Java source files using dir /s /b
set "SOURCES_FILE=%TEMP%\blindhire_sources_%RANDOM%.txt"
if exist "%SOURCES_FILE%" del "%SOURCES_FILE%"

dir /s /b "%~dp0..\backend\src\*.java" > "%SOURCES_FILE%"

echo [BUILD] Compiling Core Java backend sources...
"%JAVAC_BIN%" -encoding UTF-8 -d "%~dp0..\backend\bin" -cp "%~dp0..\backend\lib\*" @"%SOURCES_FILE%"

set COMPILE_STATUS=%errorlevel%
if exist "%SOURCES_FILE%" del "%SOURCES_FILE%"

if %COMPILE_STATUS% neq 0 (
    echo ==================================================================
    echo [ERROR] Compilation failed! Please check the error messages above.
    echo ==================================================================
    pause
    exit /b %COMPILE_STATUS%
)

echo ==================================================================
echo [SUCCESS] Backend compiled successfully into backend\bin\
echo ==================================================================
exit /b 0
