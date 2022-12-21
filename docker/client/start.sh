#!/bin/bash

#Refresh metadata
service shibd restart

httpd-foreground &
yarn install && yarn serve
