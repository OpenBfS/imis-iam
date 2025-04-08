#!/bin/sh -e

#
# Start Keycloak with example data for development setup
#

DIR=$(dirname $0)

if [ -z $KEYCLOAK_HOME ]
then
    KEYCLOAK_HOME=/opt/keycloak
fi
export PGPASSWORD=secret

# Environment variables used in import file
# Keycloak expects User Profile config as string
export USER_PROFILE="$(jq -c '.' $DIR/user_profile.json | sed 's/\\/\\\\/g;s/\"/\\\"/g')"

# Start Keycloak
${KEYCLOAK_HOME}/bin/kc.sh --verbose start --optimized --import-realm &
wait-for-it localhost:8080 -t 120 || { echo "Failed starting Keycloak"; exit 1; }

# Add example data
psql -h db -U keycloak -d keycloak -a -f ${DIR}/add_example_data.sql -w

echo "... done"

wait $!
