#!/bin/sh -e

DIR=$(dirname $0)

if [ -z $KEYCLOAK_HOME ]
then
    KEYCLOAK_HOME=/opt/keycloak
fi
export PGPASSWORD=secret

wait_for_db () {
    wait=10
    waited=0
    until psql -h iam_db -U keycloak -wl > /dev/null; do
        sleep 1
        waited=$(($waited + 1))
        if [ $waited -gt $wait ]; then
            echo "Could not connect to PostgreSQL database"
            exit 1
        fi
    done
}

# Start Keycloak
wait_for_db
${KEYCLOAK_HOME}/bin/kc.sh build
${KEYCLOAK_HOME}/bin/kc.sh start &
wait-for-it localhost:8080 -t 120 || { echo "Failed starting Keycloak"; exit 1; }

echo "Configure realm, SAML client and example account if not existing ..."
KEYCLOAK_URL=http://localhost:8080
KEYCLOAK_REALM=master
IMIS_REALM=imis3
IMIS_CLIENT=iam-client

# Get access token
TKN=$(curl -sX POST "${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token" \
 -H "Content-Type: application/x-www-form-urlencoded" \
 -d "username=${KEYCLOAK_ADMIN}" \
 -d "password=${KEYCLOAK_ADMIN_PASSWORD}" \
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
echo "Creating example user"
curl -sX POST "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/users" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TKN" \
    -d @${DIR}/iam_user.json

# Configure and sync LDAP
echo "Creating LDAP user federation"
curl -sX POST "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/components" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer $TKN" \
     -d @${DIR}/ldap_provider.json

echo "Creating client roles"
for name in "Nutzer" "Redakteur" "Chefredakteur" "technischer Administrator"
do
    curl -sX POST "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/clients/$(jq .id ${DIR}/iam_client.json | tr -d '"')/roles" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TKN" \
        -d "{\"name\": \"${name}\"}"
done

echo "... done"

#Insert attributes for exampleuser
psql -h iam_db -U keycloak -d keycloak -a -f ${DIR}/add_user_attributes.sql -w

wait $!
