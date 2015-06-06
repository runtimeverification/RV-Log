#The first argument is the path (relative to the location of this script) to the trace file.
#The second argument is the relative path to the specification file. 
rv-log $2
rv-monitor -d CustomizedLogReader/rvm/ --indexByVal $2
javac CustomizedLogReader/rvm/*.java
cd CustomizedLogReader/
java rvm.LogReader ../$1
