package com.dbs;

public class Peer {

    private static String PEER_ID;

    public static void main(String[] args) {

        if(args.length != 6) {
            System.out.println("usage: Peer <version> <server_id> <server_access_point_name> <mc_host:mc_port> <mdb_host:mdb_port> <mdr_host:mdr_port>");
            return;
        }

        final String VERSION = args[0];
        final String PEER_ID = args[1];
        final String RMI_NAME = args[2];
        final String MC_ADDRESS = args[3];
        final String MDB_ADDRESS = args[4];
        final String MDR_ADDRESS = args[5];

        PeerController peerController = new PeerController(VERSION, PEER_ID, RMI_NAME, MC_ADDRESS, MDB_ADDRESS, MDR_ADDRESS);

        peerController.start();


        //One can use peerController.close() to disconnect it before terminating the application
    }


}
