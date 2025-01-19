#!/bin/bash
# purge-image.sh $1 = camo image URL from GH

curl -X PURGE $1
