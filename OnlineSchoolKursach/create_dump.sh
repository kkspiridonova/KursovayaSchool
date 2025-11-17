#!/bin/bash
# Создание SQL-дампа базы данных (структура + данные)
DB_NAME="${1:-OnlineSchoolWeb}"
DB_USER="${2:-postgres}"
DB_PASSWORD="${3:-1}"
DB_HOST="${4:-localhost}"
DB_PORT="${5:-5432}"

mkdir -p dumps
DATETIME=$(date +%Y%m%d_%H%M%S)
DUMP_FILE="dumps/database_dump_${DATETIME}.sql"

export PGPASSWORD="$DB_PASSWORD"
pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -F p --clean --if-exists -f "$DUMP_FILE"
unset PGPASSWORD

if [ $? -eq 0 ]; then
    echo "Дамп создан: $DUMP_FILE"
else
    echo "Ошибка создания дампа!"
    exit 1
fi

