#!/bin/sh
REMOTE_IPS=10.112.103.13,10.112.103.14
REMOTE_IPS=${REMOTE_IPS//,/$'\n'}
APPNAME="RCP_Cores-1.0-SNAPSHOT.jar"
USERNAME=rcpetl
count=`ps -ef | grep $APPNAME | grep -v grep | wc -l`
if [ $count -gt 0 ];then
  ps -ef | grep $APPNAME | grep -v grep | cut -c 9-15 | xargs kill -9
fi
echo "本机 $APPNAME 进程已停止！"
# 遍历执行远程服务器部署命令
for REMOTE_IP in $REMOTE_IPS
do
  remote_count=$(ssh $USERNAME@$REMOTE_IP "ps -ef | grep $APPNAME | grep -v grep | wc -l")
  if [ $remote_count -gt 0 ];then
    ssh $USERNAME@$REMOTE_IP "ps -ef | grep $APPNAME | grep -v grep | cut -c 9-15 | xargs kill -9"
    echo "远程主机 $APPNAME 进程已停止 -> $USERNAME@$REMOTE_IP"
  fi
done