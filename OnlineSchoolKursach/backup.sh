#!/bin/bash
DB_NAME="${1:-OnlineSchoolWeb}"
DB_USER="${2:-postgres}"
DB_PASSWORD="${3:-1}"
DB_HOST="${4:-localhost}"
DB_PORT="${5:-5432}"

mkdir -p backups
DATETIME=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="backups/backup_${DB_NAME}_${DATETIME}.sql"

export PGPASSWORD="$DB_PASSWORD"
pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -F p -f "$BACKUP_FILE"
unset PGPASSWORD

if [ $? -eq 0 ]; then
    echo "Бэкап создан: $BACKUP_FILE"
else
    echo "Ошибка создания бэкапа!"
    exit 1
fi
