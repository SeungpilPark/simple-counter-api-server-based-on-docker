# Step3

## Assignment

 Query all created counters with the server API and show their current values, as shown in the following example:

```shell
$ cat list_counter.sh

#!/bin/bash
for x in $(curl -s http://<nginx-ip>/counter/); do
    curl -s http://<nginx-ip>/counter/${x}/ 
done

$ ./list_counter.sh 
{"current": 5, "to": 1000} ....

$ ./list_counter.sh | wc -l 
100
```


## Explain

There is nothing special, and it is the same as the explanation of [Step2](Step2.md).

