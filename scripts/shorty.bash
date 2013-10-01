#!/bin/bash
#  A script to test url shortening
#  Usage shorten.bash url shorty-domain
#
SHORTYDOMAIN=${2-"http://disc.org"}

if [ $# -lt 1 ]; then
  (echo
   echo Usage: `basename $0` url-to-shorten [shorty-deploy-url];
   echo "  (shorty-deploy-url defaults to $SHORTYDOMAIN)") >&2
  exit 1
fi

TESTURL=$1

RES=`curl -X POST \
       -s \
       -L \
       -H "Content-Type: application/json" \
       -d "[\"$TESTURL\"]" \
       $SHORTYDOMAIN/`

#echo Got $RES

case $RES in
  \"$SHORTYDOMAIN/* ) exit 0 ;;
  * )  echo $TESTURL FAIL WITH $RES
       exit 1 ;;
esac
