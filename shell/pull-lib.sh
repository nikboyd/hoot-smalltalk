#!/bin/bash
# pull-lib.sh ... pull JAR from cloudsmith package repository, $1=library name, $2=library type

get_deps='org.apache.maven.plugins:maven-dependency-plugin:2.8:get'
location='central::default::hoot-libs'
lib_group='hoot-smalltalk'
mvn_opts='-U -B'
if [ -d /workspace ]; then
   maven_opts+=" --settings .m2/lib-settings.xml"
fi

# fetch library version
version=$( ./query-repo.sh $1 )
lib_name="$1"
lib_type="jar"
if [ $2 ]; then lib_type="$2"; fi

echo "pulling $lib_name:$version:$lib_type"
lib_pom="$lib_name-$version.pom.xml"
lib_file="$lib_name-$version.$lib_type"
lib_folder="$HOME/.m2/repository/$lib_group/$lib_name/$version"

# pull the JAR
if [ ! $2 ]; then
mvn $mvn_opts $get_deps -DremoteRepositories=$location -Dtransitive=false \
    -DgroupId=$lib_group -DartifactId=$lib_name -Dversion=$version -Dpackaging=$lib_type
fi

if [ ! -f $lib_folder/$lib_pom ]; then
mvn $mvn_opts $get_deps -DremoteRepositories=$location -Dtransitive=false \
    -DgroupId=$lib_group -DartifactId=$lib_name -Dversion=$version -Dpackaging=pom
fi
