#!/bin/bash
# fetch-docs-bundle.sh download docs bundle and copy to jars folder

rm -fr docs/jars
mkdir -p docs/jars
docs_loc=".m2/repository/hoot-smalltalk"
docs_lib=hoot-docs-bundle
docs_version=$( shell/query-repo.sh $docs_lib )
docs_file="$docs_lib-$docs_version.jar"
shell/pull-lib.sh $docs_lib # fetch from remote repo to local
cp ~/$docs_loc/$docs_lib/$docs_version/$docs_file docs/jars/
ls -al docs/jars/
