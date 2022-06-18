#!/bin/bash
# get-cover-color.sh get library test coverage color, $1=library name

# locate coverage reports
lib_name=$1
cover_folder="$lib_name/target/site/jacoco"
cover_file="$lib_name/target/site/jacoco/index.html"

cover_percent="0"
if [ $1 ]; then
    cover_percent=$( cat $cover_file | grep -o 'Total[^%]*' | sed -e 's/Total.*ctr2">//g' )
fi

case 1 in
    $(($cover_percent > 80)))echo "brightgreen";;
    $(($cover_percent > 60)))echo "green";;
    $(($cover_percent > 40)))echo "yellow";;
    $(($cover_percent > 20)))echo "orange";;
    *)echo "red";;
esac
