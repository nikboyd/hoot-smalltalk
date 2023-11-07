#!/bin/bash
# build-cover-badges.sh build coverage badges, $1=build result

mvn_code=$1
lib_folders="hoot-compiler hoot-maven-plugin hoot-runtime libs-hoot"
for lib_name in $lib_folders; do
    shell/build-cover-badge.sh $mvn_code $lib_name
done
