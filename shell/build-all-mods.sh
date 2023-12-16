#!/bin/bash
# build-all-mods.sh build all hoot modules, $1=additional maven options

work=$( pwd )
echo "running hoot build here: $work"

stamp_test="2020.0101.0101"
stamp=$( date +"%Y.%m%d.%H%M" )
echo "pom.xml" >> .gitignore

# also see: .mvn/jvm.config
maven_opts="-U -B "
if [ $1 ]; then maven_opts+="$1"; fi

mvn $maven_opts clean

# cloud builds also do the following
if [[ $USER == runner ]]; then # change timestamp in poms
    mvn $maven_opts versions:set -DgroupId=hoot-smalltalk -DartifactId=* -DoldVersion=$stamp_test -DnewVersion=$stamp
fi

mvn $maven_opts install
mvn_code=$?

# cloud builds also do the following
if [[ $USER == runner ]]; then

    # push bundles to package registry
    shell/deploy-libs.sh $mvn_code $stamp

    # build and push version tag
    shell/build-git-tag.sh $mvn_code $stamp

fi
