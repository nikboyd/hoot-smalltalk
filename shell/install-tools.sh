#!/bin/bash
# installs common tools

apt-get update
apt-get install software-properties-common
apt-get install -y python3-pip
python3 --version
pip --version

if [ -d /workspace ]; then
    BUNDLE_TAG=$( git describe --abbrev=0 )
    echo "$BUNDLE_TAG" > /workspace/hoot-docs-bundle-version.txt
fi
