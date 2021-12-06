#!/bin/bash
# push-coverage.sh copy and push coverage reports, $1=build result

mvn_code=$1

HUB_NAME="Nik Boyd"
HUB_MAIL="nikboyd@sonic.net"
HUB_URL="https://$MAVEN_REPO_USER:$MAVEN_REPO_PASS@github.com/nikboyd/hoot-smalltalk"

git config --global user.email "$HUB_MAIL"
git config --global user.name  "$HUB_NAME"
git config remote.origin.url   "$HUB_URL"

if [[ $mvn_code == 0 ]]; then
    # push coverage reports
    git add -A
    git commit -m "generated test coverage reports"
    git push
fi
