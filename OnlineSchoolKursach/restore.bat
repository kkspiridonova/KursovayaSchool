@echo off
if "%~1"=="" (
    echo Использование: restore.bat путь_к_файлу.sql
    exit /b 1
)

set BACKUP_FILE=%~1
set DB_NAME=OnlineSchoolWeb
set DB_USER=postgres
set DB_PASSWORD=1
set DB_HOST=localhost
set DB_PORT=5432

if not exist "%BACKUP_FILE%" (
    echo Файл не найден: %BACKUP_FILE%
    exit /b 1
)

set /p CONFIRM="Восстановить базу? (yes/no): "
if /i not "%CONFIRM%"=="yes" exit /b 0

set PGPASSWORD=%DB_PASSWORD%
psql -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -f "%BACKUP_FILE%"
set PGPASSWORD=

if %ERRORLEVEL% EQU 0 (
    echo Восстановление завершено!
) else (
    echo Ошибка восстановления!
    exit /b %ERRORLEVEL%
)
