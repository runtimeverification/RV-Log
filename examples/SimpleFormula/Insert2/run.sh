rv-log --format=monpoly Insert2.rvm

rv-monitor -d CustomizedLogReader/rvm/ Insert2.rvm 

javac CustomizedLogReader/rvm/*.java

cd CustomizedLogReader

java rvm.LogReader ../violation.log
