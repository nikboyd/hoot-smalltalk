#!/bin/sh
# fetch-cover-zip.sh fetch coverage.zip from lab packages, requires PKG_VERSION

work=$( pwd )
LAB_PACKAGE="${CI_API_V4_URL}/projects/${CI_PROJECT_ID}/packages/generic/coverage/$PKG_VERSION/coverage.zip"
wget --header="JOB-TOKEN: $CI_JOB_TOKEN" $LAB_PACKAGE
