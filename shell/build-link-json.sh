#!/bin/sh
# build-link-json.sh builds asset link JSON, $1=name, $2=asset, $3=version

#PKG_VERSION=$3
PKG_REPO="https://dl.cloudsmith.io/public/educery/hoot-libs/maven/hoot-smalltalk"
JAR_LINK="$PKG_REPO/$2/$PKG_VERSION/$2-$PKG_VERSION.jar"
JAR_JSON="{'name':\"$1\", 'url':\"$JAR_LINK\", 'link_type':'package'}"
REPLACED=$( echo "$JAR_JSON" | tr "'" '"' )
JSON_TEXT="'${REPLACED}'"

echo "$REPLACED"
