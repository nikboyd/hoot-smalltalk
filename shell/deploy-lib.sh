#!/bin/bash
# deploy-lib.sh deploy built JAR to hosted artifact repo, $1=library name, $2=library type, $3=version

work=$HOME
maven_opts="-U -B --settings .m2/lib-settings.xml"
# override settings if workspace
if [ -d /workspace ]; then
   work="/workspace"
   maven_opts+=" --settings .m2/lib-settings.xml"
fi

#location='hoot-libs'
#lib_group='hoot-smalltalk'
#remote_repo="https://maven.cloudsmith.io/educery/hoot-libs/"

location='github'
lib_group='hoot-smalltalk'
remote_repo='https://maven.pkg.github.com/nikboyd/hoot-smalltalk/'

lib_name=$1
lib_type='jar'
# override type if given
if [ $2 ]; then lib_type=$2; fi

version="2020.0101.0101"
# override version if given
if [ $3 ]; then version=$3; fi

lib_pom="$lib_name-$version.pom"
lib_file="$lib_name-$version.$lib_type"
lib_home="$work/.m2/repository"
lib_folder="$lib_home/$lib_group/$lib_name/$version"

#mvn $maven_opts install:install-file \
#    -DlocalRepositoryPath=$lib_home \
#    -Dfile=$lib_name/target/$lib_file -DpomFile=$lib_name/pom.xml

mkdir -p .m2/repo-temp
cp $lib_folder/$lib_file .m2/repo-temp/
cp $lib_folder/$lib_pom  .m2/repo-temp/

mvn $maven_opts deploy:deploy-file \
    -DrepositoryId=$location -Durl=$remote_repo -Dfile=.m2/repo-temp/$lib_file \
    -DgroupId=$lib_group -DartifactId=$lib_name -Dversion=$version -Dpackaging=$lib_type
