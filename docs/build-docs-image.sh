#!/bin/bash
# build-docs-image.sh build a docker container image to host hoot-docs-bundle

docs/remove-all-runners.sh
docker rmi hoot-docs-host
docker build -t hoot-docs-host -f docs/hoot-docs-dockerfile docs
