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

if [ -d /workspace ]; then
   local_repo='/workspace/.m2/repository'
   maven_opts+=" -Dmaven.repo.local=$local_repo --settings .m2/lib-settings.xml "

   export MAVEN_REPO_USER=$( cat /workspace/hoot-secret-admin.txt      | tr -d '[:space:]' )
   export MAVEN_REPO_PASS=$( cat /workspace/hoot-secret-cloudsmith.txt | tr -d '[:space:]' )
fi

mvn $maven_opts clean
mvn $maven_opts versions:set -DgroupId=hoot-smalltalk -DartifactId=* -DoldVersion=$stamp_test -DnewVersion=$stamp
mvn $maven_opts install
mvn_code=$?

# push bundles to package registry
shell/deploy-libs.sh $mvn_code $stamp

# build and push version tag
shell/build-git-tag.sh $mvn_code $stamp

if [ -d /workspace ]; then

   # build badges after tests
   shell/build-mvn-badge.sh $mvn_code $stamp
   shell/build-cover-badges.sh $mvn_code

   # zip coverage and upload
   shell/build-cover-zip.sh $mvn_code $stamp

   # build and push version tag
   shell/build-git-tag.sh $mvn_code $stamp

   # publish coverage reports
   shell/push-cover-zip.sh $stamp
   shell/trigger-cover-pub.sh $stamp
fi
