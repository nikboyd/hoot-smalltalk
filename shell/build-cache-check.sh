#!/bin/bash
# build-cache-check.sh build a checksum file of the dependencies

# remove sums file if present
if [ -f checked-sums.txt ]; then rm checked-sums.txt; fi
if [ -f sums-check.txt ]; then rm sums-check.txt; fi

# locate pom.xml files, excluding sample-code
find . -type f \( -name "pom.xml" ! -path "*sample*" \) -print > cached-poms.txt

# generate check sums
while read pom || [[ -n $pom ]]; do
    cksum $pom >> checked-sums.txt
done < cached-poms.txt

# sort the sums, for consistency
sort -s -n -k 1,1 -o checked-sums.txt{,}
cat checked-sums.txt

# sum of checks
cksum checked-sums.txt >> sums-check.txt
cat sums-check.txt
