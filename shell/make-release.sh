#!/bin/sh
# make-release.sh create a release in project home

release_name="Hoot Smalltalk Release $PKG_VERSION"
compiler_link=$( shell/build-link-json.sh 'compiler bundle JAR' 'hoot-compiler-bundle' )
library_link=$( shell/build-link-json.sh 'library bundle JAR' 'hoot-libs-bundle' )
plugin_link=$( shell/build-link-json.sh 'compiler plugin JAR' 'hoot-maven-plugin' )

release-cli create \
    --name "$release_name" \
    --tag-name "$PKG_VERSION" \
    --assets-link "$compiler_link" \
    --assets-link "$library_link"  \
    --assets-link "$plugin_link"   \
    --description "automated release"
