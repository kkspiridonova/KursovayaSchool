@echo off
chcp 65001 >nul
echo ========================================
echo Creating database dump
echo ========================================

set DB_NAME=OnlineSchoolWeb2
set DB_USER=postgres
set DB_PASSWORD=1
set DB_HOST=localhost
set DB_PORT=5432

echo Checking database connection...
echo Database: %DB_NAME%
echo User: %DB_USER%
echo Host: %DB_HOST%:%DB_PORT%

REM Search for pg_dump in standard PostgreSQL installation paths
set PG_DUMP_PATH=
where pg_dump >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    set PG_DUMP_PATH=pg_dump
    goto :found_pg_dump
)

REM Search in standard PostgreSQL installation paths
for /d %%P in ("C:\Program Files\PostgreSQL\*") do (
    if exist "%%P\bin\pg_dump.exe" (
        set PG_DUMP_PATH=%%P\bin\pg_dump.exe
        goto :found_pg_dump
    )
)

REM Search in Program Files (x86)
for /d %%P in ("C:\Program Files (x86)\PostgreSQL\*") do (
    if exist "%%P\bin\pg_dump.exe" (
        set PG_DUMP_PATH=%%P\bin\pg_dump.exe
        goto :found_pg_dump
    )
)

REM If not found
echo ERROR: pg_dump not found!
echo.
echo Try one of the following:
echo 1. Add PostgreSQL to PATH (recommended)
echo    - Open "Environment Variables" in Windows
echo    - Add PostgreSQL bin folder path to PATH
echo    - Example: C:\Program Files\PostgreSQL\15\bin
echo.
echo 2. Or specify full path to pg_dump in the script
echo.
pause
exit /b 1

:found_pg_dump
echo Found pg_dump: %PG_DUMP_PATH%

REM Create dumps folder
if not exist "dumps" (
    echo Creating dumps folder...
    mkdir "dumps"
)

REM Generate filename with date and time
for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value') do set datetime=%%I
set DATETIME=%datetime:~0,8%_%datetime:~8,6%
set DUMP_FILE=dumps\database_dump_%DB_NAME%_%DATETIME%.sql

echo.
echo Creating dump...
echo File: %DUMP_FILE%
echo Note: Dump includes --clean flag (will drop objects before recreating)
echo.

REM Execute pg_dump with --clean flag
set PGPASSWORD=%DB_PASSWORD%
"%PG_DUMP_PATH%" -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -F p --clean --if-exists -f "%DUMP_FILE%" 2>&1
set DUMP_ERROR=%ERRORLEVEL%
set PGPASSWORD=

echo.

if %DUMP_ERROR% EQU 0 (
    if exist "%DUMP_FILE%" (
        echo ========================================
        echo SUCCESS! Dump created: %DUMP_FILE%
        echo ========================================
        dir "%DUMP_FILE%"
        echo.
        echo This dump can be used to recreate the database from scratch.
        echo It includes DROP statements before CREATE statements.
    ) else (
        echo ERROR: File was not created, although command completed successfully
    )
) else (
    echo ========================================
    echo ERROR creating dump!
    echo ========================================
    echo.
    echo Possible reasons:
    echo 1. Database "%DB_NAME%" does not exist
    echo 2. Wrong password or username
    echo 3. PostgreSQL is not running
    echo 4. Connection problems with server
    echo.
    pause
    exit /b %DUMP_ERROR%
)

pause
