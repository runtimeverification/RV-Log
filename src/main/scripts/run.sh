#!/bin/bash

# The below incantation sets SRC_ROOT to be the canonicalized directory of this script
SRC_ROOT=$(
cd -P -- "$(dirname -- "$0")" &&
printf '%s\n' "$(pwd -P)/$(basename -- "$0")"
) || exit 1
SRC_ROOT=`dirname "$SRC_ROOT"`

RELEASE="$SRC_ROOT/rvmLib"
export CLASSPATH="$RELEASE/rv-monitor.jar:$RELEASE/rv-monitor-rt.jar:$CLASSPATH"

#The first argument is the path (relative to the location of this script) to the trace file.
#The second argument is the relative path to the specification file. 
#The options -events, -witness and -trace can be given afterwards in any order.

TraceFile=$1
SpecFile=$2
shift;
shift;

rv-log $SpecFile >/dev/null
rv-monitor -t -d CustomizedLogReader/rvm/ --indexByVal $@ $SpecFile >/dev/null
javac CustomizedLogReader/rvm/*.java
cd CustomizedLogReader/
java rvm.LogReader ../$TraceFile
