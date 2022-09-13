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
${KEYCLOAK_HOME}/bin/kc.sh start &
wait-for-it localhost:8080 -t 120 || { echo "Failed starting Keycloak"; exit 1; }

echo "Configure realm, SAML client and example account if not existing ..."
KEYCLOAK_URL=http://localhost:8080
KEYCLOAK_REALM=master
IMIS_REALM=imis3
IMIS_CLIENT=iam-client
IMIS_CLIENT_ID=$(jq .id ${DIR}/iam_client.json | tr -d '"')

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
echo "Creating example users"
for user in "iam_user.json" "iam_redakteur.json" "iam_chefredakteur.json" "iam_admin.json"
do
    curl -sX POST "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/users" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TKN" \
        -d @${DIR}/${user}
done

# Configure and sync LDAP
echo "Creating LDAP user federation"
curl -sX POST "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/components" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer $TKN" \
     -d @${DIR}/ldap_provider.json

echo "Creating client roles"
for name in "Nutzer" "Redakteur" "Chefredakteur" "technischer Administrator"
do
    curl -sX POST "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/clients/$IMIS_CLIENT_ID/roles" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TKN" \
        -d "{\"name\": \"${name}\"}"
done

roles=$(curl -sX GET "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/clients/$IMIS_CLIENT_ID/roles" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TKN")
users=$(curl -sX GET "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/users" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TKN")

echo "Assigning client roles"
#Assign Nutzer role
nutzerRole=$(echo $roles | jq 'map(select(.name=="Nutzer"))')
nutzerUser=$(echo $users | jq 'map(select(.username=="exampleuser")) | .[0]')

curl -sX POST "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/users/$(echo $nutzerUser | jq .id | tr -d '"')/role-mappings/clients/$IMIS_CLIENT_ID" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TKN" \
        -d "$nutzerRole"

#Assign Redakteur role
redRole=$(echo $roles | jq 'map(select(.name=="Redakteur"))')
redUser=$(echo $users | jq 'map(select(.username=="redakteur")) | .[0]')
curl -sX POST "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/users/$(echo $redUser | jq .id | tr -d '"')/role-mappings/clients/$IMIS_CLIENT_ID" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TKN" \
        -d "$redRole"
#Assign Chefredakteur role
chefRedRole=$(echo $roles | jq 'map(select(.name=="Chefredakteur"))')
chefRedUser=$(echo $users | jq 'map(select(.username=="chefredakteur")) | .[0]')
curl -sX POST "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/users/$(echo $chefRedUser | jq .id | tr -d '"')/role-mappings/clients/$IMIS_CLIENT_ID" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TKN" \
        -d "$chefRedRole"
#Assign tech. Admin role
adminRole=$(echo $roles | jq 'map(select(.name=="technischer Administrator"))')
adminUser=$(echo $users | jq 'map(select(.username=="techadmin")) | .[0]')
curl -sX POST "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/users/$(echo $adminUser | jq .id | tr -d '"')/role-mappings/clients/$IMIS_CLIENT_ID" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TKN" \
        -d "$adminRole"

echo "... done"

#Insert attributes for exampleuser
psql -h iam_db -U keycloak -d keycloak -a -f ${DIR}/add_user_attributes.sql -w

wait $!
