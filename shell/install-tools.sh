#!/bin/bash
# installs common tools

apt-get update
apt-get install software-properties-common
python3 --version
python3 -m ensurepip --default-pip
pip --version

#sudo apt-get update
#sudo apt-get install -y maven
