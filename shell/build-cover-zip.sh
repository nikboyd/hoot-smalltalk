#!/bin/bash
# build-cover-zip.sh build a zip with all the coverage reports, $1=build result, $2=version

mvn_code=$1
stamp=$2

lib_folders="hoot-runtime hoot-compiler hoot-maven-plugin libs-hoot"
for lib_name in $lib_folders; do
    zip -rq coverage.zip $lib_name/target/site/
done
