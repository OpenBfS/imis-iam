#!/bin/bash

#Refresh metadata
service shibd restart

rm -f /run/apache2/apache2.pid
apache2ctl -D FOREGROUND &
yarn install && yarn serve --public ${SP_HOSTNAME} --port ${SP_PORT}
