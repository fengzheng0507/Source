#!/bin/sh
REMOTE_IPS=10.112.103.13,10.112.103.14
REMOTE_IPS=${REMOTE_IPS//,/$'\n'}
CURDIR=$(pwd)
PARENT_DIR=${CURDIR%/*}
APPNAME="RCP_Cores-1.0-SNAPSHOT.jar"
USERNAME=rcpetl
./stop.sh
# 以部署本机为主用节点，启动预处理程序
cd boot
java -jar -Xms1024m -Xmx6144m -Xss512k -XX:PermSize=512m -XX:MaxPermSize=2048m -XX:+UseConcMarkSweepGC $APPNAME &
# 遍历执行远程服务器部署命令
for REMOTE_IP in $REMOTE_IPS
do
  ssh $USERNAME@$REMOTE_IP "cd $CURDIR/boot;nohup java -jar -Xms1024m -Xmx6144m -Xss512k -XX:PermSize=512m -XX:MaxPermSize=2048m -XX:+UseConcMarkSweepGC $APPNAME >/dev/null 2>&1 &"
done