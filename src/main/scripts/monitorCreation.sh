#!/bin/bash

# The below incantation sets SRC_ROOT to be the canonicalized directory of this script
SRC_ROOT=$(
cd -P -- "$(dirname -- "$0")" &&
printf '%s\n' "$(pwd -P)/$(basename -- "$0")"
) || exit 1
SRC_ROOT=`dirname "$SRC_ROOT"`

RELEASE="$SRC_ROOT/rvmLib"
export CLASSPATH="$RELEASE/rv-monitor.jar:$RELEASE/rv-monitor-rt.jar:$CLASSPATH"

#The first argument is the path to the specification file. 
#The options -events, -witness and -trace can be given afterwards in any order.

SpecFile=$1
shift;

#The '-t' option is provided to both rv-log and rv-monitor for specification 'T3B2.rvm'
TimeProp=""
if [[ $SpecFile == *"T3B2.rvm"* ]]
then TimeProp="-t"
fi

$SRC_ROOT/rv-log $TimeProp $SpecFile >/dev/null
rv-monitor -t -d CustomizedLogReader/rvm/ --indexByVal $TimeProp $@ $SpecFile >/dev/null
javac CustomizedLogReader/rvm/*.java

