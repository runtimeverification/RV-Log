#The first argument is the path (relative to the location of this script) to the trace file.
#The second argument is the relative path to the specification file. 
#The options -events, -witness and -trace can be given afterwards in any order.

TraceFile=$1
SpecFile=$2
shift;
shift;

rv-log $SpecFile
rv-monitor -t -d CustomizedLogReader/rvm/ --indexByVal $@ $SpecFile
javac CustomizedLogReader/rvm/*.java
cd CustomizedLogReader/
java rvm.LogReader ../$TraceFile
