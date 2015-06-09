#The first argument is the path (relative to the location of this script) to the trace file.
#The second argument is the relative path to the specification file. 
#The options -events, -witness and -trace can be given afterwards in any order.

RVM_ARGS=""
for var in "$@"
do
   if ["$var" == "-events"]
   then RVM_ARGS="$RVM_ARGS $var"

   elif ["$var" == "-witness"]
   then RVM_ARGS="$RVM_ARGS $var"

   elif ["$var" == "-trace"]
   then RVM_ARGS="$RVM_ARGS $var"
   
   else 
	:
   fi
   
done

rv-log $2
rv-monitor -t -d CustomizedLogReader/rvm/ $RVM_ARGS --indexByVal $2
javac CustomizedLogReader/rvm/*.java
cd CustomizedLogReader/
java rvm.LogReader ../$1
