#!/bin/bash
# build-mvn-badge.sh build a badge showing maven pass or fail, $1=build result, $2=version

mvn_code=$1
version=$2
if [ ! $2 ]; then version="$HOOT_VERSION"; fi

# locate coverage reports
cover_folder="hoot-runtime/target/site/jacoco"
cover_file="hoot-runtime/target/site/jacoco/index.html"

# did maven build pass or fail? generate badge
mvn_label="hoot-$version"
mvn_color="red" && [[ $mvn_code == 0 ]] && mvn_color="green"
mvn_state="failed" && [[ $mvn_code == 0 ]] && mvn_state="passed"
mvn_url="https://raster.shields.io/static/v1?label=$mvn_label&message=$mvn_state&color=$mvn_color"
if [ -d $cover_folder ]; then
    curl $mvn_url > $cover_folder/maven_badge.png
fi
