#!/bin/bash
# copy-coverage.sh build coverage reports, $1=build result

mvn_code=$1

if [[ $mvn_code == 0 ]]; then
    lib_folders="hoot-compiler hoot-maven-plugin hoot-runtime libs-hoot"
    for lib_name in $lib_folders; do
        cover_folder="$lib_name/target/site/jacoco"
        cover_file="$lib_name/target/site/jacoco/index.html"

        # remove prior report if present
        if [[ -d docs/$lib_name ]]; then rm -R docs/$lib_name/ ; fi

        # copy library test coverage report
        cp -R $cover_folder docs/$lib_name/
    done
fi
