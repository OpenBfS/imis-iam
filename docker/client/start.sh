#!/bin/bash

#Refresh metadata
service shibd restart

rm -f /run/apache2/apache2.pid
apache2ctl -D FOREGROUND &
yarn install
if [[ "$CLIENT_MODE" == "development" ]]; then
  yarn dev --host 0.0.0.0 --port ${PORT} --base ${CLIENT_PATH}
else
  yarn build --base ${CLIENT_PATH}
  yarn serve --host 0.0.0.0 --port ${PORT} --base ${CLIENT_PATH}
fi
