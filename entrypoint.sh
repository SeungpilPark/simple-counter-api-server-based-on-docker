#!/bin/bash

# /usr/bin/dockerd --storage-driver vfs -H unix:///var/run/docker.sock -H tcp://127.0.0.1:2375 &

# bash -c 'while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' localhost:2375/_ping)" != "200" ]]; do sleep 1; done'

echo "Docker boot end"

cd /project

node index.js &

docker-compose build
docker-compose up --force-recreate