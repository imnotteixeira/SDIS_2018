#!/usr/bin/env bash
for (( I = 0; I < 10; ++I )); do
    
    ./start_network.sh 5
    x-terminal-emulator -e "java -cp out/production/DBS_Project/ com.dbs.TestApp"
    sleep $1
    ./kill_network.sh


    rm -rf ./*_backup_*

done
