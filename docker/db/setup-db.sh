#!/bin/bash -e

DIR=$(dirname $0)

ROLE_NAME=keycloak
ROLE_PW=secret
DB_NAME=keycloak
SCHEMA_NAME=keycloak

function psql_command() {
    SQL=${1}
    DB=${2:-postgres}
    psql -qv ON_ERROR_STOP=on -d $DB -c "$SQL"
}

psql_command "CREATE ROLE $ROLE_NAME LOGIN PASSWORD '$ROLE_PW'"

psql_command "CREATE DATABASE $DB_NAME"

psql_command "CREATE SCHEMA $SCHEMA_NAME AUTHORIZATION $ROLE_NAME" $DB_NAME
