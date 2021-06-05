#!/bin/sh
REMOTE_IPS=10.112.103.13,10.112.103.14
REMOTE_IPS=${REMOTE_IPS//,/$'\n'}
CURDIR=$(pwd)
PARENT_DIR=${CURDIR%/*}
APPNAME="RCP_Cores-1.0-SNAPSHOT.jar"
USERNAME=rcpetl
DUMP_PATH="${CURDIR}/logDir"
APP_LOG_PATH="${CURDIR}/task.log"
STOP_SCRIPT="sh stop.sh"
#START_SCRIPT="nohup java -jar -Xms1024m -Xmx6144m -Xss512k -XX:+UseConcMarkSweepGC  -XX:MaxTenuringThreshold=14 -XX:ConcGCThreads=20 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${DUMP_PATH} ${APPNAME} >${APP_LOG_PATH} 2>&1 &"
#START_SCRIPT="java -jar -Xms1024m -Xmx6144m -Xss512k -XX:+UseConcMarkSweepGC  -XX:MaxTenuringThreshold=14 -XX:ConcGCThreads=20 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/sse/users/rcpetl/rcp_preprocess/logDir  RCP_Cores-1.0-SNAPSHOT.jar"
START_SCRIPT="nohup java -jar -Xms1024m -Xmx6144m -Xss512k -XX:+UseConcMarkSweepGC  -XX:MaxTenuringThreshold=14 -XX:ConcGCThreads=20 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/sse/users/rcpetl/rcp_preprocess/logDir RCP_Cores-1.0-SNAPSHOT.jar >/sse/users/rcpetl/rcp_preprocess/logDir/task.log 2>&1 &"

# 以部署本机为主用节点，启动预处理程序
cd ${CURDIR}/boot
#nohup java -jar -Xms1024m -Xmx6144m -Xss512k -XX:+UseConcMarkSweepGC  -XX:MaxTenuringThreshold=14 -XX:ConcGCThreads=20 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${DUMP_PATH} ${APPNAME}  > ${CURDIR}/task.log 2>&1 &
`nohup java -jar -Xms1024m -Xmx6144m -Xss512k -XX:+UseConcMarkSweepGC  -XX:MaxTenuringThreshold=14 -XX:ConcGCThreads=20 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/sse/users/rcpetl/rcp_preprocess/logDir RCP_Cores-1.0-SNAPSHOT.jar >/sse/users/rcpetl/rcp_preprocess/logDir/task.log 2>&1 &` &

ps -ef | grep $APPNAME

echo "------this node started!"

# 遍历执行远程服务器部署命令
for REMOTE_IP in $REMOTE_IPS
do
  ssh $USERNAME@${REMOTE_IP} "cd ${CURDIR};$STOP_SCRIPT"
  #  ssh $USERNAME@${REMOTE_IP} "cd ${CURDIR}/boot;$START_SCRIPT "
  ssh $USERNAME@${REMOTE_IP} "cd ${CURDIR}/boot;nohup java -jar -Xms1024m -Xmx6144m -Xss512k -XX:+UseConcMarkSweepGC  -XX:MaxTenuringThreshold=14 -XX:ConcGCThreads=20 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/sse/users/rcpetl/rcp_preprocess/logDir RCP_Cores-1.0-SNAPSHOT.jar >/sse/users/rcpetl/rcp_preprocess/logDir/task.log 2>&1 &"

  echo "${REMOTE_IP} started!"

done