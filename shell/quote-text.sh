#!/bin/bash
# quote-text.sh wraps text with paired quotes, $1=text, $2=quote if present

QUOTE="'"
if [ $2 ]; then QUOTE="$2"; fi

quoted=$QUOTE
quoted+=$1
quoted+=$QUOTE
echo "$quoted"
