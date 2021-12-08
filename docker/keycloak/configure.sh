#!/bin/bash
# Script for the inital keycloak setup:
# - Import a realm for the imis applications
# - Create an openid client with enabled service accounts to access the admin api without user credentials
# - Create a saml client for the iam web application
# - Add an initial user


KEYCLOAK_URL=http://localhost:8080/auth
KEYCLOAK_REALM=master
KEYCLOAK_CLIENT_ID=clientId
KEYCLOAK_CLIENT_SECRET=clientSecret
KEYCLOAK_ADMIN_USER=admin
KEYCLOAK_ADMIN_PW=secret
IMIS_REALM=imis3

#Get access token
TKN=$(curl -X POST "${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token" \
 -H "Content-Type: application/x-www-form-urlencoded" \
 -d "username=${KEYCLOAK_ADMIN_USER}" \
 -d "password=${KEYCLOAK_ADMIN_PW}" \
 -d 'grant_type=password' \
 -d 'client_id=admin-cli' | jq -r '.access_token')

#Import imis3 realm
REALM_CONFIG=$(sed "s/%IMIS_REALM/${IMIS_REALM}/g" realm_config.json)
echo "Creating imis realm '$IMIS_REALM'"
curl -X POST "${KEYCLOAK_URL}/admin/realms" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TKN" \
    --data "$REALM_CONFIG"

#Create openid service client for service account access
echo "Creating service client: $IMIS_REALM->$(jq .clientId service_client.json)"
curl -X POST "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/clients" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TKN" \
    -d @service_client.json

#Create saml client for the client web application
echo "Creating iam client: $IMIS_REALM->$(jq .clientId iam_client.json)"
curl -X POST "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/clients" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TKN" \
    -d @iam_client.json

#Add inital user
KEYCLOAK_IAM_USER=$(jq .username iam_user.json)
KEYCLOAK_IAM_PW=$(jq '.credentials[0].value' iam_user.json)
echo "Creating iam user: $IMIS_REALM->$KEYCLOAK_IAM_USER - $KEYCLOAK_IAM_PW"
curl -X POST "${KEYCLOAK_URL}/admin/realms/$IMIS_REALM/users" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TKN" \
    -d @iam_user.json