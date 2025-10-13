#!/bin/bash

if [ ! -f ${KEYCLOAK_HOME}/nstarts ] ; then echo 0 > ${KEYCLOAK_HOME}/nstarts; fi
nstarts=$(($(cat ${KEYCLOAK_HOME}/nstarts) + 1))
echo $nstarts  > ${KEYCLOAK_HOME}/nstarts
if [ ! -f ${KEYCLOAK_HOME}/laststart ] ; then echo 0 > ${KEYCLOAK_HOME}/laststart; fi
laststart=$(cat ${KEYCLOAK_HOME}/laststart)
laststartform=$(echo $laststart | sed 's/^[0-9]*$/FORMOK/')
if [ "$laststartform" != "FORMOK" ]; then laststart=$(date +%s); fi

if [ $(($(date +%s) - $laststart)) -le  120 ] ; then
  echo "start $nstarts ($(date)): last start was less than 2 minutes ago (at $(date -d @$laststart)) - waiting for 3 minutes"
  echo $(date +%s) > ${KEYCLOAK_HOME}/laststart
  sleep 180
  echo "waiting finished - service should start now"
  #next start after a long start is also always fast
else
  echo "start $nstarts ($(date)): last start was more than 2 minutes ago (at $(date -d @$laststart)) - no delay"
  echo $(date +%s) > ${KEYCLOAK_HOME}/laststart
fi

# Start Keycloak via exec to override the bash process:
exec ${KEYCLOAK_HOME}/bin/kc.sh start --optimized
