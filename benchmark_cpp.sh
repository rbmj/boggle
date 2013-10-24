#!/bin/bash

#do benchmark
for i in $(seq 0 1000); do
    ./OnePlayer $i | tail -n1
done | cut '-d ' -f1 | awk '{ sum += $1 } END { print sum }'

