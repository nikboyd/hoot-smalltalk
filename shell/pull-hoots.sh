#!/bin/bash
# pull-hoots.sh ... pull Hoot library JARs from cloudsmith

hoot_libs="hoot-compiler-bundle hoot-libs-bundle hoot-maven-plugin"
for lib_name in $hoot_libs; do
    shell/pull-lib.sh "$lib_name"
done
