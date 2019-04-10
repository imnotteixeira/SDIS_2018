package com.dbs;

import com.dbs.Database.ChunkInfoStorer;
import com.dbs.filemanager.FileManager;

public class Peer {

    public static String PEER_ID;
    public static String VERSION;
    public static String RMI_NAME;
    public static String MC_ADDRESS;
    public static String MDB_ADDRESS;
    public static String MDR_ADDRESS;

    public static void main(String[] args) {

        if(args.length != 6) {
            System.out.println("usage: Peer <version> <server_id> <server_access_point_name> <mc_host:mc_port> <mdb_host:mdb_port> <mdr_host:mdr_port>");
            return;
        }

        VERSION = args[0];
        PEER_ID = args[1];
        RMI_NAME = args[2];
        MC_ADDRESS = args[3];
        MDB_ADDRESS = args[4];
        MDR_ADDRESS = args[5];

        PeerController peerController = PeerController.getInstance();

        FileManager.createPeerFileTree();
        ChunkInfoStorer.loadFromFile();

        peerController.start();


        //One can use peerController.close() to disconnect it before terminating the application
    }


}
