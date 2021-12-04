#!/bin/bash
# push-coverage.sh copy and push coverage reports, $1=build result

mvn_code=$1

HUB_NAME="Nik Boyd"
HUB_MAIL="nikboyd@sonic.net"
HUB_URL="https://$MAVEN_REPO_USER:$MAVEN_REPO_PASS@github.com/nikboyd/hoot-smalltalk"

git config --global user.email "$HUB_MAIL"
git config --global user.name  "$HUB_NAME"
git config remote.origin.url "$HUB_URL"
git stash

if [[ $mvn_code == 0 ]]; then
    lib_folders="hoot-runtime hoot-compiler hoot-maven-plugin libs-hoot"
    for lib_name in $lib_folders; do
        cover_folder="$lib_name/target/site/jacoco"
        cover_file="$lib_name/target/site/jacoco/index.html"

        if [[ -d docs/$lib_name ]]; then rm -R docs/$lib_name/ ; fi
        cp -R $cover_folder docs/$lib_name/
    done

    git add -A
    git commit -m "generated test coverage reports"
    git push
fi
