#!/bin/bash
# push-coverage.sh copy and push coverage reports, $1=build result

mvn_code=$1

if [[ $mvn_code == 0 ]]; then

lib_folders="hoot-runtime hoot-compiler hoot-maven-plugin libs-hoot"
for lib_name in $lib_folders; do
    if [[ ! -d docs/$lib_name ]]; then mkdir docs/$lib_name; fi

    cover_folder="$lib_name/target/site/jacoco"
    cover_file="$lib_name/target/site/jacoco/index.html"

    cp $cover_folder docs/$lib_name/
done

fi
