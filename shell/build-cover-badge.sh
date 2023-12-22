#!/bin/bash
# build-cover-badge.sh build a badge showing test coverage percent, $1=build result, $2=library name, $3=full name

mvn_code=$1
lib_name=$2
lib_label=$( echo "$2" | awk -F- '{print $NF}' )
lib_label+="%20coverage"
if [ $3 ]; then lib_label="$3%20coverage"; fi
#echo "$lib_label"

# locate coverage reports
cover_folder="$lib_name/target/site/jacoco"
cover_file="$lib_name/target/site/jacoco/index.html"

# gather test coverage percent, generate badge
# note %25 = '%' sign escaped for URL
if [[ $mvn_code == 0 ]]; then
    cover_percent=$( shell/get-cover-percent.sh $lib_name )
    cover_color=$( shell/get-cover-color.sh $lib_name )
else
    cover_percent="0%25"
    cover_color=$( shell/get-cover-color.sh )
fi

cover_url="https://raster.shields.io/badge/coverage-$cover_percent-$cover_color?label=$lib_label"
curl $cover_url > $cover_folder/coverage_badge.png
#echo "$cover_url"
