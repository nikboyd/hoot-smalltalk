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
mvn $maven_opts versions:set -DgroupId=hoot-smalltalk -DartifactId=* -DoldVersion=$stamp_test -DnewVersion=$stamp
mvn $maven_opts install
mvn_code=$?

# cloud builds also do the following
if [[ $USER == runner ]]; then

    # push bundles to package registry
    shell/deploy-libs.sh $mvn_code $stamp

    # clear build changes from file system
    git stash -u

    # build badges after tests
    shell/build-mvn-badge.sh $mvn_code $stamp
    shell/build-cover-badges.sh $mvn_code

    # copy coverage reports
    shell/copy-coverage.sh $mvn_code
    ls docs/hoot-runtime/

    # push test coverage reports
    shell/push-coverage.sh $mvn_code

    # build and push version tag
    shell/build-git-tag.sh $mvn_code $stamp

fi
