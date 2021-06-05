#!/bin/bash

targetTopic=(0001 0002 0003 0004 0005 0006 0020 0991)

sortedPrefix="rcps-sse.mtp.cnmf."
ordBookPrefix="rcps-sse.mtp.ordbook."
postfix=".sorted"

delPrefix="kafka-topics.sh --delete --topic "
delPostfix=" --zookeeper rcpjt002:2181,rcpjt003:2181,rcpjt004:2181/kafka"

crePrefix="kafka-topics.sh --create --topic "
crePostfix="  --partitions 1 --replication-factor 2 --zookeeper rcpjt002:2181,rcpjt003:2181,rcpjt004:2181/kafka"

# 列出删除前的topic
echo `kafka-topics.sh --list --zookeeper rcpjt002:2181,rcpjt003:2181,rcpjt004:2181/kafka`
echo "------------------------"
echo "------------ initialize start ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓"

# 先删除所有目标topic
for i in ${targetTopic[@]} ; do
  sortedTopicName=${sortedPrefix}${i}${postfix}
  ordBookTopicName=${ordBookPrefix}${i}${postfix}

  delSorted=${delPrefix}${sortedTopicName}${delPostfix}
  creSorted=${crePrefix}${sortedTopicName}${crePostfix}

  delOrdBook=${delPrefix}${ordBookTopicName}${delPostfix}
  creOrdBook=${crePrefix}${ordBookTopicName}${crePostfix}

   echo "do->["$delSorted"]"
   echo `$delSorted`
   echo "do->["$delOrdBook"]"
   echo `$delOrdBook`

#   echo "do->["$creSorted"]"
#   echo `$creSorted`
#   echo "do->["$creOrdBook"]"
#   echo `$creOrdBook`

done

# 列出初始化之后的topic
echo "------------------------"
echo `kafka-topics.sh --list --zookeeper rcpjt002:2181,rcpjt003:2181,rcpjt004:2181/kafka`
echo "------------ initialize end ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑"