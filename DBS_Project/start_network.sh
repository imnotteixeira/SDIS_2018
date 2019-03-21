#!/usr/bin/env bash


ARGC=$#
if [[ ${ARGC} -eq 0 ]]
then
    echo "usage: start_network.sh <n_peers> <MC> <MDB> <MDR>"
    exit 1
fi

if [[ ${ARGC} -eq 1 ]]
then
    NUM_PEERS=$1
    MC=220.220.220.0:8765
    MDB=220.220.220.0:8766
    MDR=220.220.220.0:8767
fi

if [[ ${ARGC} -eq 4 ]]
then
    NUM_PEERS=$1
    MC=$2
    MDB=$3
    MDR=$4
fi

find ./src/com/dbs/ -name "*.java" > sources.txt
javac -d ./out/production/DBS_Project/ @sources.txt

for (( I = 0; I < ${NUM_PEERS}; ++I )); do
    ((id=I+1))
    x-terminal-emulator -e "java -cp out/production/DBS_Project/ com.dbs.Peer 1.0 $id peer_$id $MC $MDB $MDR"
done