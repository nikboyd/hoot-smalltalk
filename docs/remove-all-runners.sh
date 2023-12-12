#!/bin/bash
# shell/remove-all-runners.sh ... running containers

count=$(docker ps -a -q | wc -l)
if [[ $count -gt 0 ]]; then
   docker rm $(docker ps -a -q)
fi
