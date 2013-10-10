#!/bin/bash

#do not include jvm startup in benchmark
java OnePlayer 1111 > /dev/null

#do benchmark
for i in $(seq 0 1000); do
    java OnePlayer $i | tail -n1
done | cut '-d ' -f1 | awk '{ sum += $1 } END { print sum }'

