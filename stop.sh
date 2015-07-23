#!/bin/bash
cur="`dirname "$0"`"
cur="`cd "$cur"; pwd`"
lib=${cur}/target/webapi-0.0.1/WEB-INF/lib

for file in ${lib}/*.jar
do
proc_name="$classpath":"$file"
done
name_suffixx="\>"
proc_id=`ps -ef|grep -i ${proc_name}${name_suffixx}|grep -v "grep"|awk '{print $2}'`
if [[ -z $proc_id ]];then
    echo "The task is not running! "
else
     for id in ${proc_id[*]}
     do
       echo PID:${id}
       thread=`ps -mp ${id}|wc -l`
       kill -9 ${id}

       if [ $? -eq 0 ];then

            echo "task is killed."
       else
            echo "kill task failed."
       fi
     done
fi
