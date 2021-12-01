#!/bin/bash
# trigger-cover-pub.sh trigger lab coverage report publication, $1=version

work=$( pwd )
REF_NAME="dev-ops"
LAB_HOME="10019964"
LAB_TOKEN=$( cat $work/hoot-secret-trigger.txt | tr -d '[:space:]' )
LAB_TRIGGER="https://gitlab.com/api/v4/projects/$LAB_HOME/trigger/pipeline"
curl -X POST -F "token=$LAB_TOKEN" -F "ref=$REF_NAME" -F "variables[PKG_VERSION]=$1" $LAB_TRIGGER
