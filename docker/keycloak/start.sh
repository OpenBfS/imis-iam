#!/bin/bash

if [ ! -f ${KEYCLOAK_HOME}/nstarts ] ; then echo 0 > ${KEYCLOAK_HOME}/nstarts; fi
nstarts=$(($(cat ${KEYCLOAK_HOME}/nstarts) + 1))
echo $nstarts  > ${KEYCLOAK_HOME}/nstarts
if [ ! -f ${KEYCLOAK_HOME}/laststart ] ; then echo 0 > ${KEYCLOAK_HOME}/laststart; fi
if [ $(($(date +%s) - $(cat ${KEYCLOAK_HOME}/laststart))) -le  120 ] ; then
  echo "start $nstarts: last start was less than 2 minutes ago - waiting for 3 minutes"
  echo $(date +%s) > ${KEYCLOAK_HOME}/laststart
  sleep 180
  echo "waiting finished - service should start now"
  #next start after a long start is also always fast
else
  echo "start $nstarts: last start was more than 2 minutes ago - no delay"
  echo $(date +%s) > ${KEYCLOAK_HOME}/laststart
fi


# Start Keycloak via exec to override the bash process:
exec ${KEYCLOAK_HOME}/bin/kc.sh start --optimized
