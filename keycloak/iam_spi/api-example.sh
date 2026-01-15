#!/bin/bash

# Authentication fallback values
KEYCLOAK_URL="http://localhost:48081"
REALM="imis3"
CLIENT_ID="iam-client"
CLIENT_SECRET="exampleclientsecret"
USERNAME="exampleuser"
PASSWORD="secret"

# Read more specific values from docker config
source "$(dirname $(readlink -f "$0"))/../../docker/dev.env"

# Authenticate: Get the access token
echo "Authenticating..."
token_response=$(curl -s -d "client_id=${CLIENT_ID}" \
  -d "client_secret=${CLIENT_SECRET}" \
  -d "grant_type=password" \
  -d "username=${USERNAME}" \
  -d "password=${PASSWORD}" \
  "${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token")

access_token=$(echo "$token_response" | jq -r '.access_token')

if [ "$access_token" = "null" ] || [ -z "$access_token" ]; then
  echo "Authentication failed"
  echo "$token_response" | jq .
  exit 1
fi

echo "Authentication successful"

# List users
echo "Fetching users..."
curl -s --fail -H "Authorization: Bearer $access_token" \
  "${KEYCLOAK_URL}/realms/${REALM}/iam/user?maxResults=10" | jq .