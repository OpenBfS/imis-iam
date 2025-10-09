#!/bin/sh -e

#
# Start Keycloak with example data for development setup
#

DIR=$(dirname $0)

# Add example data
${DIR}/import_realm.sh --verbose --override false
export PGPASSWORD=secret
psql -h db -U keycloak -d keycloak -a -f ${DIR}/add_example_data.sql -w

# Start Keycloak
exec ${KEYCLOAK_HOME}/bin/kc.sh --verbose start --optimized
