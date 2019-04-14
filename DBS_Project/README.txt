There are two entry points in the project: Peer and TestApp, for a Peer instance and a Client application, respectively.

To compile, run the following:

find ./src/com/dbs/ -name "*.java" > sources.txt
javac -d ./out/production/DBS_Project/ @sources.txt

This places all sources in a file sources.txt, to be compiled by javac

Then, to run a Peer:
java -cp out/production/DBS_Project/ com.dbs.Peer <version> <id> <peer_id> <MC> <MDB> <MDR>

To run a TestApp, it's a similar approach:
java -cp out/production/DBS_Project/ com.dbs.TestApp <peer_ap> <operation> <op1> <op2>

A helper script (start_network.sh) was created to launch multiple peers on the same machine for debug purposes
To start 5 peers:
./start_network 5 <PEER_VERSION> <MC> <MDB> <MDR>

This launches 5 peers, each on a new terminal (tested on UBUNTU 18.04, with GNOME)


Another helper script (kill_network.sh) was created to close all opened peers, instead of having to ^C each of them individually

There are already 3 files to test the application,
 - a small txt file - testfile.txt - 1 chunk
 - a bigger txt file test.txt - 3 chunks
 - a huge image - 8ktest.jpg