#!/usr/bin/env bash


ARGC=$#
if [[ ${ARGC} -ne 5 && ${ARGC} -ne 2 ]]
then
    echo "usage: start_network.sh <n_peers> <version> <MC> <MDB> <MDR>"
    exit 1
fi

if [[ ${ARGC} -eq 2 ]]
then
    NUM_PEERS=$1
    VERSION=$2
    echo "MULTICAST CHANNELS NOT SPECIFIED, USING DEFAULTS!"
    MC=230.0.0.0:8765
    MDB=230.0.0.0:8766
    MDR=230.0.0.0:8767
    echo "MC = 230.0.0.0:8765"
    echo "MC = 230.0.0.0:8766"
    echo "MC = 230.0.0.0:8767"
fi

if [[ ${ARGC} -eq 5 ]]
then
    NUM_PEERS=$1
    VERSION=$2
    MC=$3
    MDB=$4
    MDR=$5
fi

find ./src/com/dbs/ -name "*.java" > sources.txt
javac -d ./out/production/DBS_Project/ @sources.txt

for (( I = 0; I < ${NUM_PEERS}; ++I )); do
    ((id=I+1))
    x-terminal-emulator -e "java -cp out/production/DBS_Project/ com.dbs.Peer $VERSION $id peer_$id $MC $MDB $MDR"
done
