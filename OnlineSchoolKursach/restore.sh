#!/bin/bash
if [ -z "$1" ]; then
    echo "Использование: ./restore.sh путь_к_файлу.sql"
    exit 1
fi

BACKUP_FILE="$1"
DB_NAME="${2:-OnlineSchoolWeb}"
DB_USER="${3:-postgres}"
DB_PASSWORD="${4:-1}"
DB_HOST="${5:-localhost}"
DB_PORT="${6:-5432}"

if [ ! -f "$BACKUP_FILE" ]; then
    echo "Файл не найден: $BACKUP_FILE"
    exit 1
fi

read -p "Восстановить базу? (yes/no): " CONFIRM
if [ "$CONFIRM" != "yes" ]; then
    exit 0
fi

export PGPASSWORD="$DB_PASSWORD"
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -f "$BACKUP_FILE"
unset PGPASSWORD

if [ $? -eq 0 ]; then
    echo "Восстановление завершено!"
else
    echo "Ошибка восстановления!"
    exit 1
fi
