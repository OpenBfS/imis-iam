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
${KEYCLOAK_HOME}/bin/kc.sh --verbose start --optimized &
wait-for-it localhost:8080 -t 120 || { echo "Failed starting Keycloak"; exit 1; }

echo "Configure realm, SAML client and example account if not existing ..."
KEYCLOAK_URL=http://localhost:8080
KEYCLOAK_REALM=master
IMIS_REALM=imis3
IMIS_CLIENT=iam-client
IMIS_CLIENT_ID=$(jq .id ${DIR}/iam_client.json | tr -d '"')

# Try to get access token or wait until the initial admin user was added
counter=1
max_retry=10
TKN="null"
while [ "$TKN" = "null" -a "$counter" != "$max_retry" ]
do
    echo "Trying to get acess token..."
    TKN=$(curl -sX POST "${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=${KEYCLOAK_ADMIN}" \
    -d "password=${KEYCLOAK_ADMIN_PASSWORD}" \
    -d 'grant_type=password' \
    -d 'client_id=admin-cli' | jq -r '.access_token')
    counter=$((counter+1))
    sleep 2
done

# Import imis3 realm
REALM_CONFIG=$(sed "s/%IMIS_REALM/${IMIS_REALM}/g" ${DIR}/realm_config.json)
echo "Creating imis realm '$IMIS_REALM'"
curl -sX POST "${KEYCLOAK_URL}/admin/realms" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TKN" \
    --data "$REALM_CONFIG"

# Create SAML client for the client web application
echo "Creating iam client: $IMIS_REALM->$(jq .clientId ${DIR}/iam_client.json)"
SP_URL="${PROTOCOLL}://${SP_HOSTNAME}:${SP_PORT}"
CLIENT_CONFIG=$(sed "s@%SP_URL@${SP_URL}@g" ${DIR}/iam_client.json)
curl -sX POST "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/clients" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TKN" \
    --data "$CLIENT_CONFIG"

# Configure and sync LDAP
echo "Creating LDAP user federation"
curl -sX POST "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/components" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer $TKN" \
     -d @${DIR}/ldap_provider.json

${KEYCLOAK_HOME}/bin/kcadm.sh config credentials \
    --server http://localhost:8080 --realm master \
    --user $KEYCLOAK_ADMIN --password $KEYCLOAK_ADMIN_PASSWORD

echo "Creating user profile"
${KEYCLOAK_HOME}/bin/kcadm.sh update realms/$IMIS_REALM/users/profile \
    -f ${DIR}/user_profile.json

echo "Creating example users"
for user in "iam_user.json" "iam_redakteur.json" "iam_chefredakteur.json" "iam_admin.json"
do
    ${KEYCLOAK_HOME}/bin/kcadm.sh create users -r $IMIS_REALM -f ${DIR}/${user}
done

echo "Creating client roles"
for name in "chief_editor" "editor" "user"
do
    # Create roles with name and key for localization
    role_id=$(${KEYCLOAK_HOME}/bin/kcadm.sh create \
        clients/$IMIS_CLIENT_ID/roles -r "$IMIS_REALM" \
            -s name="$name" -s description="role_iam_$name" -o | \
        jq ".id" | tr -d '"')
    if [ "$comp_role_id" ]; then
        # Make previously created role a composite role by adding the new one
        ${KEYCLOAK_HOME}/bin/kcadm.sh add-roles \
            -r "$IMIS_REALM" --cclientid "$IMIS_CLIENT" \
            --rid "$comp_role_id" \
            --rolename "$name"
    fi
    comp_role_id="$role_id"
done

roles=$(curl -sX GET "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/clients/$IMIS_CLIENT_ID/roles" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TKN")
users=$(curl -sX GET "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/users" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TKN")
groups=$(curl -sX GET "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/groups" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TKN")
defaultGroupId=$(echo $groups | jq 'map(select(.name=="Landesadmin")) | .[0]' | jq .id | tr -d '"')

echo "Assigning groups and client roles"

#Assign user role
userRole=$(echo $roles | jq 'map(select(.name=="user"))')
userId=$(echo $users | jq 'map(select(.username=="exampleuser")) | .[0]' | jq .id | tr -d '"')

curl -sX POST "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/users/$userId/role-mappings/clients/$IMIS_CLIENT_ID" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TKN" \
        -d "$userRole"
curl -sX PUT "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/users/$userId/groups/$defaultGroupId" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TKN"

#Assign editor role
editorRole=$(echo $roles | jq 'map(select(.name=="editor"))')
editorUserId=$(echo $users | jq 'map(select(.username=="redakteur")) | .[0]' | jq .id | tr -d '"')

curl -sX POST "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/users/$editorUserId/role-mappings/clients/$IMIS_CLIENT_ID" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TKN" \
        -d "$editorRole"
curl -sX PUT "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/users/$editorUserId/groups/$defaultGroupId" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TKN"

#Assign chief_editor role
chiefEditorRole=$(echo $roles | jq 'map(select(.name=="chief_editor"))')
chiefEditorUserId=$(echo $users | jq 'map(select(.username=="chefredakteur")) | .[0]' | jq .id | tr -d '"')

curl -sX POST "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/users/$chiefEditorUserId/role-mappings/clients/$IMIS_CLIENT_ID" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TKN" \
        -d "$chiefEditorRole"
curl -sX PUT "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/users/$chiefEditorUserId/groups/$defaultGroupId" \
        -H "Content-Type: application/json" \
        -H "Authorization: Bearer $TKN"

#Insert attributes for exampleuser
psql -h iam_db -U keycloak -d keycloak -a -f ${DIR}/add_user_attributes.sql -w
#Insert and assign example institutions
psql -h iam_db -U keycloak -d keycloak -a -f ${DIR}/add_example_institutions.sql -w

echo "... done"

wait $!
