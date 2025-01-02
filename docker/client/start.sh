#!/bin/bash

#Refresh metadata
service shibd restart

rm -f /run/apache2/apache2.pid
apache2ctl -D FOREGROUND &
yarn install
if [[ "$CLIENT_MODE" == "development" ]]; then
  yarn dev --host ${CLIENT_HOST} --port ${SP_PORT}
else
  yarn build
  yarn serve --host ${CLIENT_HOST} --port ${SP_PORT}
fi
