#!/bin/bash
# build-docs-image.sh build a docker container image to host hoot-docs-bundle

docs/remove-all-runners.sh
docker rmi hoot-docs-host

mkdir -p docs/jars
cp ./hoot-docs-bundle/target/*.jar docs/jars/
docker build -t hoot-docs-host -f docs/hoot-docs-dockerfile docs
