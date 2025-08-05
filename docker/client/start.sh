#!/bin/bash

set -eu -o pipefail

rm -f /run/apache2/apache2.pid

if [[ "${CLIENT_MODE:-production}" == "development" ]]; then
    sed -i "$ a Include /usr/local/iamdocker/dev.conf" /usr/local/apache2/conf/httpd.conf
    # Install in case a clean checkout is bind-mounted in dev setup
    yarn install && yarn dev --host 0.0.0.0 --port 8080 --base ${CLIENT_PATH} &
fi

exec httpd-foreground
