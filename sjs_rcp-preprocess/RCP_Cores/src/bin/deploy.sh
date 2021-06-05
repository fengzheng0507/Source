#!/bin/sh
REMOTE_IPS=10.112.103.13,10.112.103.14
REMOTE_IPS=${REMOTE_IPS//,/$'\n'}
CURDIR=$(pwd)
PARENT_DIR=${CURDIR%/*}
TARNAME="rcp_cores-RCP_Cores.tar.gz"
APPNAME="RCP_Cores-1.0-SNAPSHOT.jar"
USERNAME=rcpetl
# 以部署本机为主用节点，启动预处理程序
#if [ ! -e $CURDIR/rcp_cores/ ]; then rm -rf $CURDIR/rcp_cores/; fi
#tar -zxf $TARNAME
cd boot
java -jar -Xms1024m -Xmx6144m -Xss512k -XX:PermSize=512m -XX:MaxPermSize=2048m -XX:+UseConcMarkSweepGC $APPNAME &
# 遍历执行远程服务器部署命令
for REMOTE_IP in $REMOTE_IPS
do
 ssh $USERNAME@$REMOTE_IP "if [ ! -e $PARENT_DIR ]; then mkdir $PARENT_DIR; fi"
 scp -r $PARENT_DIR/$TARNAME $USERNAME@$REMOTE_IP:$PARENT_DIR/$TARNAME
 ssh $USERNAME@$REMOTE_IP "cd $PARENT_DIR;tar -zxf $TARNAME"
 ssh $USERNAME@$REMOTE_IP "cd $CURDIR/boot;nohup java -jar -Xms1024m -Xmx6144m -Xss512k -XX:PermSize=512m -XX:MaxPermSize=2048m -XX:+UseConcMarkSweepGC $APPNAME >/dev/null 2>&1 &"
done