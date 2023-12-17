#!/bin/bash
# installs common tools

apt-get update
apt-get install software-properties-common
apt-get install -y python3-pip
python3 --version
pip --version
