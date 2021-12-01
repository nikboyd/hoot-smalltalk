#!/bin/bash
# get-hoot-version.sh get version from libs-hoot.jar

work=$( pwd )
version=$( ls $work/libs-hoot/target/libs*.jar | awk -F "target/libs-hoot-" '{print $2}' | awk -F ".jar" '{print $1}' )
echo "$version"
