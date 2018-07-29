# Step6

## Assignment

 Create counters again with create_counter.sh as in step 2C) and implement a wait_counter.sh script that will wait for all counters to expire.
 (wait_counter.sh logic does not need to be implemented in shell, it can call any language of choice)

```shell
$ ./create_counter.sh

$ ./wait_counter.sh

$ ./list_counter.sh | wc -l 0
```


## Explain

There is nothing special, and it is the same as the explanation of [Step2](Step2.md).

**Simple shell script for await rest api**

`wait_counter.sh` 

```shell

#!/bin/bash

NGINX_HOST=http://localhost:80

#----------------------------------------------------------------------
# 카운터 종료 대기

echo "Waiting counter complete....."

# 최대 1 시간 동안 기다리기. 1200 * 3s = 1200s = 60min
MAX_COUNT=1200
INTERVAL=3
CURRENT_COUNT=0
ERR_COUNT=0

while true
do
  COUNTS="$(curl --request GET \
              -H 'content-type: text/plain' \
              ${NGINX_HOST}/counter/counts-all)"

  HTTP_STATUS="$(curl --request GET \
                -s -o /dev/null -w "%{http_code}" \
                -H 'content-type: text/plain' \
                ${NGINX_HOST}/counter/counts-all)"

  if [ $HTTP_STATUS -eq 200 ];then

     echo "que $CURRENT_COUNT : $COUNTS counters are running"

     #----------------------------------------------------------------------
     # 카운터가 모두 종료되었을 경우

     if [ $COUNTS -eq 0 ];then
        echo "All counter  completed!!"
        break
     fi

     #----------------------------------------------------------------------
     # 호출 카운트 증가

     CURRENT_COUNT=$((CURRENT_COUNT + 1))
     ERR_COUNT=0

     #----------------------------------------------------------------------
     # 타임아웃이 걸렸을 경우

     if [ "$CURRENT_COUNT" -gt "$MAX_COUNT" ];then
        echo "Time out. Restart script to continue counter monitoring.";
        exit 1
     fi
     sleep 3

  else
     echo "Failed to get counters."

     #----------------------------------------------------------------------
     # 에러 카운트 증가

     ERR_COUNT=$((ERR_COUNT + 1))

     #----------------------------------------------------------------------
     # 에러가 30초 이상 지속시 종료

     if [ $ERR_COUNT -gt 10 ];then
        echo "Failed to connecting ngnix. pleas check out your ngnix network environment."
        exit 1
     fi

     sleep 3

  fi
done
```

