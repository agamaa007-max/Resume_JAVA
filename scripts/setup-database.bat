@echo off
setlocal

echo ==================================================================
echo       BlindHire - MySQL Database Initializer & Setup Script
echo ==================================================================

:: Check if mysql CLI is available
where mysql >nul 2>&1
if %errorlevel% neq 0 (
    echo [NOTE] 'mysql' CLI command not found in current PATH.
    echo Searching standard MySQL Server installation directories...
    if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
        set "MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
    ) else if exist "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" (
        set "MYSQL_BIN=C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe"
    ) else (
        echo [INFO] You do not need to run this script manually!
        echo The Java backend (DatabaseInitializer.java) will automatically create
        echo the 'blindhire' database and tables when you start the server via compile-and-run.bat.
        pause
        exit /b 0
    )
) else (
    set "MYSQL_BIN=mysql"
)

echo [INFO] Found MySQL binary: %MYSQL_BIN%
echo.
set /p DB_USER="Enter MySQL Username [default: root]: "
if "%DB_USER%"=="" set DB_USER=root

set /p DB_PASS="Enter MySQL Password (press Enter if blank): "

echo.
echo Initializing 'blindhire' schema and sample synthetic data...

if "%DB_PASS%"=="" (
    "%MYSQL_BIN%" -u %DB_USER% < "%~dp0..\database\schema.sql"
    "%MYSQL_BIN%" -u %DB_USER% < "%~dp0..\database\sample-data.sql"
) else (
    "%MYSQL_BIN%" -u %DB_USER% -p%DB_PASS% < "%~dp0..\database\schema.sql"
    "%MYSQL_BIN%" -u %DB_USER% -p%DB_PASS% < "%~dp0..\database\sample-data.sql"
)

if %errorlevel% equ 0 (
    echo ==================================================================
    echo [SUCCESS] Database 'blindhire' schema and sample data initialized!
    echo ==================================================================
) else (
    echo [WARNING] MySQL CLI returned status code %errorlevel%.
    echo Note that the Java backend will also attempt automatic initialization on boot.
)

pause
