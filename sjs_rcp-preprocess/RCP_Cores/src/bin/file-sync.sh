#!/bin/bash

paramnum=$#
if((paramnum==0));then
  echo no params;
  exit;
fi

p1=$1
file_name=`basename $p1`
echo fname=$file_name

pdir=`cd -P $(dirname $p1); pwd`
echo pdir=$pdir

user=`whoami`
# hostPrefix=`rcpjt0`

for (( host = 12; host < 15; host++ )); do
    echo --------------rcpjt0$host------------------
    rsync -rvl $pdir/$file_name $user@rcpjt0$host:$pdir
done
