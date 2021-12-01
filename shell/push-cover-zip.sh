#!/bin/bash
# push-cover-zip.sh push coverage.zip up to lab packages, $1=version

stamp=$1
work=$( pwd )
LAB_HOME="10019964"
LAB_TOKEN=$( cat $work/hoot-secret-gitlab.txt | tr -d '[:space:]' )
LAB_REMOTE="https://gitlab.com/api/v4/projects/$LAB_HOME/packages/generic/coverage/$stamp/coverage.zip"
curl --header "PRIVATE-TOKEN: $LAB_TOKEN" --upload-file $work/coverage.zip "$LAB_REMOTE"
