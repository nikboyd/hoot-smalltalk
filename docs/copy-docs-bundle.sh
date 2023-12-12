#!/bin/bash
# copy-docs-bundle.sh copy to docs/jars

rm -fr docs/jars
mkdir -p docs/jars
docs_loc=".m2/repository/hoot-smalltalk"
docs_lib=hoot-docs-bundle
docs_version=$1
docs_file="$docs_lib-$docs_version.jar"
cp ~/$docs_loc/$docs_lib/$docs_version/$docs_file docs/jars/
ls -al docs/jars/
