# Step5

## Assignment

 Implement an API call to delete the counter, and delete all existing counters as shown below:

```shell
$ ./create_couter.sh

$ for x in $(curl -s http://<nginx-ip>/counter/); do curl -X POST http://<nginx-ip>/counter/${x}/stop/ ; done

$ ./list_counter.sh | wc -l 0
```


## Explain

There is nothing special, and it is the same as the explanation of [Step2](Step2.md).

