#!/bin/bash
# build-git-tag.sh build a git commit version tag, $1=build result, $2=version

mvn_code=$1
version=""
if [ $2 ]; then
   version+="$2"
else
   version+=$( shell/get-hoot-version.sh )
fi

work=$( pwd )
LAB_NAME="Nik Boyd"
LAB_MAIL="nikboyd@sonic.net"
echo "version = $version"

git config --global user.email "$LAB_MAIL"
git config --global user.name  "$LAB_NAME"

# generate version tag if build passed
if [[ $mvn_code == 0 ]]; then
    LAB_SECRET=$( cat $work/hoot-secret-gitlab.txt | tr -d '[:space:]' )
    git config remote.origin.url "https://hoot-smalltalk:$LAB_SECRET@gitlab.com/hoot-smalltalk/hoot-smalltalk"

    git tag -a $version -m "built by pipeline"
    git push origin --tags
fi
