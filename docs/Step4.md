# Step4

## Assignment

Dynamically change the number of API application servers:

- Reduce the number of API application servers to 0

- Then increase it to 5

- Execute step 3 again

The counters generated in step 2) must to be preserved even when no API application servers exist

```shell
$ ./setup_api.sh 0

$ curl http://<nginx-ip>/
503 Service Unavailable

$ ./setup_api.sh 5
$ for x in `seq 1 100`; do curl -s http://<nginx-ip>/ ; done | sort | uniq host1

host2
host3
host4
host5

$ ./list_counter.sh | wc -l
100
```

## Things to ask when testing

After execute `setup_api.sh` , please wait until all counter services are ready. 
You can check this [http://localhost:8761](http://localhost:8761).

If you do not have enough resources in your test environment to increase the number of instances, jvm will temporarily consume a lot of cpu and memory resources at boot time, causing the system to hang.
Thank you for your understanding in grading.

## Explain

*Q. Reduce the number of API application servers to 0, Then increase it to 5.*

There is nothing special, and it is the same as the explanation of [Step1](Step1.md).

*Q. The counters generated in step 2) must to be preserved even when no API application servers exist.*

**This is a question about understanding the concept of api-gateway response cache.**

***Flow***
![](image/cache.png) 

1. Per 5 seconds, gateway refresh alive counter-services list from eureka.
2. When request started, check if all server is down.
3. If yes, gateway load cache from redis.
4. If no, gateway proxy to counter-service
5. After proxy, if 200 ok
3. Sends a message immediately to the Kafka for each stream data.
4. All microservices receive kafka message and actual operation code is executed.


