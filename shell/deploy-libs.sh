#!/bin/bash
# deploy-libs.sh deploy built JARs to package registry, $1=build result, $2=version

mvn_code=$1
version=""
if [ $2 ]; then
   version+="$2"
else
   version+=$( shell/get-hoot-version.sh )
fi

# publish built JARs if build passed
if [[ $mvn_code == 0 ]]; then
    hoot_libs="hoot-compiler-boot hoot-maven-plugin hoot-compiler-bundle hoot-libs-bundle hoot-docs-bundle"
    for lib_name in $hoot_libs; do
        shell/deploy-lib.sh "$lib_name" jar $version
    done
fi
