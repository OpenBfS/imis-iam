#!/bin/bash -e

DIR=$(dirname $0)

# Environment variable used in import file
# Keycloak expects User Profile config as string
export USER_PROFILE="$(jq -c '.' $DIR/user_profile.json | sed 's/\\/\\\\/g;s/\"/\\\"/g')"

${KEYCLOAK_HOME}/bin/kc.sh import --optimized --file ${DIR}/data/realm.json $@
