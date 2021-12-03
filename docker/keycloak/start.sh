#!/bin/sh

cd ${KEYCLOAK_HOME}/bin
./add-user-keycloak.sh -u admin -p secret
${KEYCLOAK_HOME}/bin/standalone.sh -b 0.0.0.0 -bmanagement=0.0.0.0
