#!/bin/bash
# run-docs-image.sh run a docker container image to host hoot-docs-bundle

base=$(pwd)
docker run -p 8080:8080 -it hoot-docs-host
