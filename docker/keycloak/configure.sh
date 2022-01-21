#!/bin/bash -e
# Script for the inital keycloak setup:
# - Import a realm for the imis applications
# - Create an openid client with enabled service accounts to access the admin api without user credentials
# - Create a saml client for the iam web application
# - Add an initial user

DIR=$(dirname $0)

if [ -z $KEYCLOAK_HOME ]
then
    KEYCLOAK_HOME=/opt/keycloak
fi
JBOSS_CLI=$KEYCLOAK_HOME/bin/jboss-cli.sh

KEYCLOAK_ADMIN_USER=admin
KEYCLOAK_ADMIN_PW=secret


# Add admin users for container and keycloak web application
${KEYCLOAK_HOME}/bin/add-user.sh --silent $KEYCLOAK_ADMIN_USER $KEYCLOAK_ADMIN_PW
${KEYCLOAK_HOME}/bin/add-user-keycloak.sh -r master \
    -u $KEYCLOAK_ADMIN_USER -p $KEYCLOAK_ADMIN_PW

# Configure PostgreSQL JDBC driver and data source
$JBOSS_CLI --file=${DIR}/commands.cli
