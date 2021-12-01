#!/bin/bash
# installs jdk 8 + maven

sudo apt-get update
sudo apt-get install software-properties-common
sudo add-apt-repository ppa:openjdk-r/ppa
sudo apt-get install -y openjdk-8-jdk-headless

sudo apt-get update
sudo apt-get install -y maven
