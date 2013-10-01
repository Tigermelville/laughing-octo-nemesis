#!/bin/bash
ERRORFILE=/tmp/failure.$$.shortytest
trap "rm -Rf $ERRORFILE" 0

SHORTYDOMAIN=${2:-"http://disc.org"}

if [ $# -lt 1 ]; then
  ( echo; echo Usage: `basename $0` loop-count [shorty-deploy-url]
   echo "  (shorty-deploy-url defaults to $SHORTYDOMAIN)") >&2
  exit 1
fi

LOOPS=$1

rm -f $ERRORFILE

PORT0=11000
PORT1=11001
PORT2=11002
PORT3=11003

shorty () {
 ( 
   CMD="shorty.bash http://stackoverflow.com/$1/just-another-newbie-question $SHORTYDOMAIN:$2"
   VAL=`$CMD`
   case $? in
    0 ) ;;
    * ) echo >> $ERRORFILE
        echo CMD: $CMD >> $ERRORFILE
        echo VAL: $VAL >> $ERRORFILE ;;
   esac
 )
}

i=0

while [ $i -lt $LOOPS ]; do
  RAND=$[ 1000 + $[ RANDOM % 1000 ]]
  
  shorty $RAND $PORT0&
  shorty $RAND $PORT1&
  shorty $RAND $PORT2&
  shorty $RAND $PORT3&

  i=$[ 1 + $i ]
done

if [ -f $ERRORFILE ]; then
  echo FAILURE
  cat $ERRORFILE
  exit 1
else
  echo SUCCESS
  exit 0
fi
