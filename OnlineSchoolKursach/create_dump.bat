@echo off
REM Создание SQL-дампа базы данных (структура + данные)
set DB_NAME=OnlineSchoolWeb
set DB_USER=postgres
set DB_PASSWORD=1
set DB_HOST=localhost
set DB_PORT=5432

if not exist "dumps" mkdir "dumps"
for /f "tokens=2 delims==" %%I in ('wmic os get localdatetime /value') do set datetime=%%I
set DATETIME=%datetime:~0,8%_%datetime:~8,6%
set DUMP_FILE=dumps\database_dump_%DATETIME%.sql

set PGPASSWORD=%DB_PASSWORD%
pg_dump -h %DB_HOST% -p %DB_PORT% -U %DB_USER% -d %DB_NAME% -F p --clean --if-exists -f "%DUMP_FILE%"
set PGPASSWORD=

if %ERRORLEVEL% EQU 0 (
    echo Дамп создан: %DUMP_FILE%
) else (
    echo Ошибка создания дампа!
    exit /b %ERRORLEVEL%
)

