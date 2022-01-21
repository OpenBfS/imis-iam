#!/bin/sh -e

DIR=$(dirname $0)

if [ -z $KEYCLOAK_HOME ]
then
    KEYCLOAK_HOME=/opt/keycloak
fi

wait_for_server () {
    wait=10
    waited=0
    until ${KEYCLOAK_HOME}/bin/jboss-cli.sh -c "ls /deployment"; do
        sleep 1
        waited=$(($waited + 1))
        if [ $waited -gt $wait ]; then
            echo "Failed starting Keycloak"
            exit 1
        fi
    done
}

# Start Keycloak
${KEYCLOAK_HOME}/bin/standalone.sh -b 0.0.0.0 -bmanagement=0.0.0.0 &
wait_for_server

echo "Configure realm, SAML client and example account if not existing ..."
KEYCLOAK_URL=http://localhost:8080/auth
KEYCLOAK_REALM=master
KEYCLOAK_ADMIN_USER=admin
KEYCLOAK_ADMIN_PW=secret
IMIS_REALM=imis3
IMIS_CLIENT=iam-client

# Get access token
TKN=$(curl -sX POST "${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token" \
 -H "Content-Type: application/x-www-form-urlencoded" \
 -d "username=${KEYCLOAK_ADMIN_USER}" \
 -d "password=${KEYCLOAK_ADMIN_PW}" \
 -d 'grant_type=password' \
 -d 'client_id=admin-cli' | jq -r '.access_token')

# Import imis3 realm
REALM_CONFIG=$(sed "s/%IMIS_REALM/${IMIS_REALM}/g" ${DIR}/realm_config.json)
echo "Creating imis realm '$IMIS_REALM'"
curl -sX POST "${KEYCLOAK_URL}/admin/realms" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TKN" \
    --data "$REALM_CONFIG"

# Create SAML client for the client web application
echo "Creating iam client: $IMIS_REALM->$(jq .clientId ${DIR}/iam_client.json)"
curl -sX POST "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/clients" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TKN" \
    -d @${DIR}/iam_client.json

# Add inital user
KEYCLOAK_IAM_USER=$(jq .username ${DIR}/iam_user.json)
KEYCLOAK_IAM_PW=$(jq '.credentials[0].value' ${DIR}/iam_user.json)
echo "Creating iam user: $IMIS_REALM->$KEYCLOAK_IAM_USER - $KEYCLOAK_IAM_PW"
curl -sX POST "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/users" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TKN" \
    -d @${DIR}/iam_user.json

echo "... done"
wait $!
