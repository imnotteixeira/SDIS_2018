package com.dbs;

import com.dbs.filemanager.FileManager;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Peer {


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



        final String BACKUP_DIR = "./peer_backup_"+PEER_ID;


        System.out.println("[" + PEER_ID + "] Starting Peer...");

        System.out.println("[" + PEER_ID + "] Started Peer");



        FileManager fm = new FileManager();


        System.out.println("Saving test file...");
        byte[] data = {'a','b','c','d','e','a','b','c','d','e','a','b','c','d','e','a','b','c','d','e'};
        String fileId = "file_uniqueid12312asdas";
        fm.saveFile(BACKUP_DIR, fileId, data, 5);
        System.out.println("Test file saved successfully!");



        PeerRemoteObject peer = new PeerRemoteObject();
        IPeerInterface stub = null;

        try {
            stub = (IPeerInterface) UnicastRemoteObject.exportObject(peer, 0);

            try {
                Registry reg = LocateRegistry.getRegistry(1099);

                reg.rebind(RMI_NAME, stub);

            } catch (RemoteException e) {

                Registry reg = LocateRegistry.createRegistry(1099);

                reg.rebind(RMI_NAME, stub);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        System.out.println("Peer Disconnected. Press Enter to exit.");
        new java.util.Scanner(System.in).nextLine();
    }
}
