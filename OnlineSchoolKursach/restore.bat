@echo off
chcp 65001 >nul
echo ========================================
echo Restoring database from backup
echo ========================================

if "%~1"=="" (
    echo.
    echo Usage: restore.bat path_to_file.sql
    echo.
    echo Example:
    echo   restore.bat backups\backup_OnlineSchoolWeb2_20251118_014345.sql
    echo.
    pause
    exit /b 1
)

set BACKUP_FILE=%~1
set DB_NAME=OnlineSchoolWeb2
set DB_USER=postgres
set DB_PASSWORD=1
set DB_HOST=localhost
set DB_PORT=5432

echo Backup file: %BACKUP_FILE%
echo Database: %DB_NAME%
echo.

if not exist "%BACKUP_FILE%" (
    echo ERROR: File not found: %BACKUP_FILE%
    echo.
    echo Make sure:
    echo 1. Correct path to file is specified
    echo 2. File exists
    echo.
    pause
    exit /b 1
)

REM Search for psql in standard PostgreSQL installation paths
set PSQL_PATH=
where psql >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    set PSQL_PATH=psql
    goto :found_psql
)

REM Search in standard PostgreSQL installation paths
for /d %%P in ("C:\Program Files\PostgreSQL\*") do (
    if exist "%%P\bin\psql.exe" (
        set PSQL_PATH=%%P\bin\psql.exe
        goto :found_psql
    )
)

REM Search in Program Files (x86)
for /d %%P in ("C:\Program Files (x86)\PostgreSQL\*") do (
    if exist "%%P\bin\psql.exe" (
        set PSQL_PATH=%%P\bin\psql.exe
        goto :found_psql
    )
)

REM If not found
echo ERROR: psql not found!
echo.
echo Try to add PostgreSQL to PATH or specify full path in the script
echo.
pause
exit /b 1

:found_psql
echo Found psql: %PSQL_PATH%
echo.

REM Check if database exists
echo Checking if database exists...
set PGPASSWORD=%DB_PASSWORD%
"%PSQL_PATH%" -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -lqt 2>nul | findstr /C:"%DB_NAME%" >nul
set DB_EXISTS=%ERRORLEVEL%
set PGPASSWORD=

if %DB_EXISTS% NEQ 0 (
    echo.
    echo WARNING: Database "%DB_NAME%" does not exist!
    echo.
    echo Create database now? (type: yes)
    set /p CREATE_DB=
    if /i "%CREATE_DB%"=="yes" (
        echo Creating database...
        set PGPASSWORD=%DB_PASSWORD%
        "%PSQL_PATH%" -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d postgres -c "CREATE DATABASE \"%DB_NAME%\";" 2>&1
        set PGPASSWORD=
        if %ERRORLEVEL% NEQ 0 (
            echo ERROR: Failed to create database!
            pause
            exit /b 1
        )
        echo Database created!
    ) else (
        echo Cancelled. Create database manually:
        echo psql -U postgres -c "CREATE DATABASE \"%DB_NAME%\";"
        pause
        exit /b 0
    )
)

echo.
echo ========================================
echo WARNING!
echo ========================================
echo Restore will DELETE all current data in database "%DB_NAME%"
echo and replace it with data from file "%BACKUP_FILE%"
echo.
set /p CONFIRM="Continue restore? (yes/no): "
if /i not "%CONFIRM%"=="yes" (
    echo Cancelled.
    pause
    exit /b 0
)

echo.
echo Restoring database...
echo.

set PGPASSWORD=%DB_PASSWORD%
"%PSQL_PATH%" -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -f "%BACKUP_FILE%" 2>&1
set RESTORE_ERROR=%ERRORLEVEL%
set PGPASSWORD=

echo.

if %RESTORE_ERROR% EQU 0 (
    echo ========================================
    echo SUCCESS! Database restored!
    echo ========================================
) else (
    echo ========================================
    echo ERROR restoring database!
    echo ========================================
    echo.
    echo Possible reasons:
    echo 1. Backup file is corrupted
    echo 2. Permission issues
    echo 3. PostgreSQL version incompatibility
    echo.
    pause
    exit /b %RESTORE_ERROR%
)

pause
