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
echo Step 1: Terminating active connections to database...
set PGPASSWORD=%DB_PASSWORD%
"%PSQL_PATH%" -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = '%DB_NAME%' AND pid <> pg_backend_pid();" 2>&1
set PGPASSWORD=
echo Active connections terminated (if any).
echo.

echo Step 2: Dropping all objects in public schema...
echo This ensures clean restoration without conflicts...
set PGPASSWORD=%DB_PASSWORD%
"%PSQL_PATH%" -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -c "DROP SCHEMA IF EXISTS public CASCADE; CREATE SCHEMA public; GRANT ALL ON SCHEMA public TO postgres; GRANT ALL ON SCHEMA public TO public;" 2>&1
set DROP_ERROR=%ERRORLEVEL%
set PGPASSWORD=
if %DROP_ERROR% NEQ 0 (
    echo WARNING: Failed to drop schema. Continuing anyway...
) else (
    echo Schema dropped and recreated successfully.
)
echo.

echo Step 3: Restoring database from backup file...
echo This may take a while...
echo IMPORTANT: Watch for any ERROR messages above!
echo.

set PGPASSWORD=%DB_PASSWORD%
REM -v ON_ERROR_STOP=1 stops execution on first error
REM Note: We removed ON_ERROR_STOP temporarily to see all errors, but you can add it back if needed
"%PSQL_PATH%" -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -f "%BACKUP_FILE%" 2>&1
set RESTORE_ERROR=%ERRORLEVEL%
set PGPASSWORD=

echo.

if %RESTORE_ERROR% EQU 0 (
    echo.
    echo Step 4: Verifying restoration...
    set PGPASSWORD=%DB_PASSWORD%
    "%PSQL_PATH%" -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -c "SELECT COUNT(*) as table_count FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE';" 2>&1
    echo.
    echo Checking data counts...
    "%PSQL_PATH%" -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -c "SELECT 'users' as table_name, COUNT(*) as row_count FROM users UNION ALL SELECT 'courses', COUNT(*) FROM courses UNION ALL SELECT 'enrollments', COUNT(*) FROM enrollments UNION ALL SELECT 'lessons', COUNT(*) FROM lessons UNION ALL SELECT 'tasks', COUNT(*) FROM tasks;" 2>&1
    set PGPASSWORD=
    echo.
    echo ========================================
    echo SUCCESS! Database restored!
    echo ========================================
    echo.
    echo Please verify your data manually by:
    echo 1. Checking the application
    echo 2. Running queries in pgAdmin
    echo.
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
