#!/bin/bash
NGINX_HOST=http://localhost:80

for x in $(curl -s ${NGINX_HOST}/counter/); do
    curl -s ${NGINX_HOST}/counter/${x}/;
    echo ''
done