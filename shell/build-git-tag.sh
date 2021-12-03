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
HUB_NAME="Nik Boyd"
HUB_MAIL="nikboyd@sonic.net"
echo "version = $version"

HUB_URL="https://$MAVEN_REPO_USER:$MAVEN_REPO_PASS@github.com/nikboyd/hoot-smalltalk"

git config --global user.email "$HUB_MAIL"
git config --global user.name  "$HUB_NAME"

# generate version tag if build passed
if [[ $mvn_code == 0 ]]; then
    git config remote.origin.url "$HUB_URL"

    git tag -a $version -m "built by pipeline"
    git push origin --tags
fi
