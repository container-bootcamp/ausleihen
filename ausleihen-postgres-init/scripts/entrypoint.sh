#!/usr/bin/env bash

#set -x

POSTGRES_DB_LOCALE=${POSTGRES_DB_LOCALE:?"POSTGRES_DB_LOCALE is required"}

POSTGRES_HOST=${POSTGRES_HOST:?"POSTGRES_HOST is required"}
POSTGRES_SUPERUSER=${POSTGRES_SUPERUSER:?"POSTGRES_SUPERUSER is required"}
POSTGRES_SUPERUSER_PASSWORD=${POSTGRES_SUPERUSER_PASSWORD:?"POSTGRES_SUPERUSER_PASSWORD is required"}
POSTGRES_DB=${POSTGRES_DB:?"POSTGRES_DB is required"}

echo ${POSTGRES_HOST}":*:*:"${POSTGRES_SUPERUSER}":"${POSTGRES_SUPERUSER_PASSWORD} > ~/.pgpass
chmod 0600 ~/.pgpass

PG_DB_EXISTS=`psql -U ${POSTGRES_SUPERUSER} -h ${POSTGRES_HOST} -tAc "SELECT EXISTS ( SELECT 1 from pg_database WHERE datname='${POSTGRES_DB}');"`
PG_TABLE_EXISTS=`psql -U ${POSTGRES_SUPERUSER} -h ${POSTGRES_HOST} -tAc "SELECT EXISTS ( SELECT 1 FROM   information_schema.tables WHERE  table_schema = 'public' AND  table_name = 'journal' AND table_catalog = '${POSTGRES_DB}');"`

PG_COLLATION_EXISTS=`psql -U ${POSTGRES_SUPERUSER} -h ${POSTGRES_HOST} -tAc "SELECT EXISTS( select 1 from pg_collation);"`
if test ${PG_COLLATION_EXISTS} == "f"; then
  psql -U ${POSTGRES_SUPERUSER} -h ${POSTGRES_HOST} -tAc "CREATE COLLATION IF NOT EXISTS german (LOCALE = '${POSTGRES_DB_LOCALE}');"
fi



if test ${PG_DB_EXISTS} == "f"; then
  psql -U ${POSTGRES_SUPERUSER} -h ${POSTGRES_HOST} -tAc "CREATE DATABASE ${POSTGRES_DB} WITH TEMPLATE = template0 ENCODING = 'UTF8' LC_COLLATE = '${POSTGRES_DB_LOCALE}' LC_CTYPE = '${POSTGRES_DB_LOCALE}';"
fi

if test ${PG_TABLE_EXISTS} == "f"; then
  psql -U ${POSTGRES_SUPERUSER} -h ${POSTGRES_HOST} -tAc -d ${POSTGRES_DB}  --file=/akka-persistence.sql
fi
