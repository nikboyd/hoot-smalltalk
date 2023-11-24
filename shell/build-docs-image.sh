#!/bin/bash
# build-docs-image.sh build a docker container image to host hoot-docs-bundle

./remove-all-runners.sh
docker rmi hoot-docs-host

mkdir -p jars
cp ../hoot-docs-bundle/target/*.jar jars/
docker build -t hoot-docs-host -f hoot-docs-dockerfile .
