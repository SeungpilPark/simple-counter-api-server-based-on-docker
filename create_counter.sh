#!/bin/sh
NGINX_HOST=http://localhost:80

for x in `seq 1 100`; do
    curl -X POST "${NGINX_HOST}/counter/?to=$(((RANDOM%1000)+1000))"
done