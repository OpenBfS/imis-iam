#!/bin/bash

rm -f /run/apache2/apache2.pid

if [[ "$CLIENT_MODE" == "development" ]]; then
    a2ensite -q dev
    # Install in case a clean checkout is bind-mounted in dev setup
    yarn install && yarn dev --host 0.0.0.0 --port 8080 --base ${CLIENT_PATH} &
fi

apache2ctl -D FOREGROUND
