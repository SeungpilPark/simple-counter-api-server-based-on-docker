#!/bin/bash
NODEJS_HOST=http://localhost:3000

curl -s ${NODEJS_HOST}?count=$1;