#!/bin/bash

#Refresh metadata
service shibd restart
apachectl start
yarn serve