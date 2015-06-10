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

$SRC_ROOT/rv-log $SpecFile >/dev/null
$SRC_ROOT/rv-monitor -t -d CustomizedLogReader/rvm/ --indexByVal $@ $SpecFile >/dev/null
javac CustomizedLogReader/rvm/*.java
