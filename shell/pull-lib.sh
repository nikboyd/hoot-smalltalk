#!/bin/bash
# pull-lib.sh ... pull JAR from github package repository, $1=library name, $2=library type, $3=library version

get_deps='org.apache.maven.plugins:maven-dependency-plugin:2.8:get'
location='central::default::github'
lib_group='hoot-smalltalk'
lib_name="$1"
lib_type="jar"
if [ $2 ]; then lib_type="$2"; fi

# set library version
lib_vers=''
if [ $3 ]; then lib_vers="$3"; fi

mvn_opts='-U -B'
local_repo=''
if [ -d /workspace ]; then # cloud build
   mvn_opts+=" --settings .m2/lib-settings.xml"
   local_repo="-Dmaven.repo.local=/workspace/.m2/repository"
   if [ ! $3 ]; then lib_vers=$( cat $1-version.txt ); fi
else # fetch lib version locally
   if [ ! $3 ]; then lib_vers=$( shell/query-repo.sh $1 ); fi
fi

echo "pulling $lib_group:$lib_name:$lib_vers:$lib_type"
lib_pom="$lib_name-$lib_vers.pom.xml"
lib_file="$lib_name-$lib_vers.$lib_type"
lib_folder="$HOME/.m2/repository/$lib_group/$lib_name/$lib_vers"

# pull the JAR
if [ ! -f $lib_folder/$lib_file ]; then
echo "$USER pulling from $location opts $mvn_opts $local_repo"
mvn $mvn_opts $get_deps $local_repo -DremoteRepositories=$location -Dtransitive=false \
    -DgroupId=$lib_group -DartifactId=$lib_name -Dversion=$lib_vers -Dpackaging=$lib_type
fi

if [ ! -f $lib_folder/$lib_pom ]; then
echo "$USER pulling from $location opts $mvn_opts $local_repo"
mvn $mvn_opts $get_deps $local_repo -DremoteRepositories=$location -Dtransitive=false \
    -DgroupId=$lib_group -DartifactId=$lib_name -Dversion=$lib_vers -Dpackaging=pom
fi
