#!/bin/bash
cur="`dirname "$0"`"
cur="`cd "$cur"; pwd`"
lib=${cur}/target/webapi-0.0.1/WEB-INF/lib

for file in ${lib}/*.jar
do
classpath="$classpath":"$file"
done
classpath="$classpath":${cur}/target/webapi-0.0.1/WEB-INF/classes/
nohup java -cp $classpath com.nana.webapi.ui.WebApi ${cur}/target/webapi-0.0.1/ > webapi.log 2>&1 < /dev/null &

