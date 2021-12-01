#!/bin/bash
# query-repo.sh ... query cloudsmith package repository, $1=library name

lib_repo='educery/hoot-libs'
lib_query="name:$1"

# list latest version of library
version=$( cloudsmith list pkgs $lib_repo -q $lib_query | sort -r -k 3 | head -3 | tail -1 | awk '{print $3}' )
echo "$version"
