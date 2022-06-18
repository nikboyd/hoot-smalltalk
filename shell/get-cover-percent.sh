#!/bin/bash
# get-cover-percent.sh get library test coverage percent, $1=library name

# locate coverage reports
lib_name=$1
cover_folder="$lib_name/target/site/jacoco"
cover_file="$lib_name/target/site/jacoco/index.html"

# note %25 = '%' sign escaped for URL
cover_percent=$( cat $cover_file | grep -o 'Total[^%]*%' | sed -e 's/Total.*ctr2">//g' )"25"
echo "$cover_percent"
