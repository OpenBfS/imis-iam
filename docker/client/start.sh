#!/bin/bash

#Refresh metadata
service shibd restart

rm -f /run/apache2/apache2.pid
apache2ctl -D FOREGROUND &
yarn install && yarn dev --host --port ${SP_PORT}
