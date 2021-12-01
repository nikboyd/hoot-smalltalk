#!/bin/bash
# build-cover-badge.sh build a badge showing test coverage percent, $1=build result, $2=library name

mvn_code=$1
lib_name=$2
lib_label=$( echo "$2" | awk -F- '{print $NF}' )
lib_label+="%20coverage"
echo "$lib_label"

# locate coverage reports
cover_folder="$lib_name/target/site/jacoco"
cover_file="$lib_name/target/site/jacoco/index.html"

# gather test coverage percent, generate badge
# note %25 = '%' sign escaped for URL
if [[ $mvn_code == 0 ]]; then
    cover_percent=$( cat $cover_file | grep -o 'Total[^%]*%' | sed -e 's/Total.*ctr2">//g' )"25"
    cover_url="https://img.shields.io/badge/coverage-$cover_percent-yellow?label=$lib_label"
    curl $cover_url > $cover_folder/coverage_badge.svg
else
    cover_url="https://img.shields.io/badge/coverage-0%25-red?label=$lib_label"
    curl $cover_url > $cover_folder/coverage_badge.svg
fi
