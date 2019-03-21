package com.dbs;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Peer {

    public static void main(String[] args) {

        if(args.length != 1) {
            System.out.println("usage: peer <peer_id>");
            return;
        }

        String peer_id = args[0];

        System.out.println("[" + peer_id + "] Starting Peer...");

        System.out.println("[" + peer_id + "] Started Peer");

        PeerRemoteObject peer = new PeerRemoteObject();
        IPeerInterface stub = null;

        try {
            stub = (IPeerInterface) UnicastRemoteObject.exportObject(peer, 0);

            try {
                Registry reg = LocateRegistry.getRegistry(1099);

                reg.rebind(peer_id, stub);

            } catch (RemoteException e) {

                Registry reg = LocateRegistry.createRegistry(1099);

                reg.rebind(peer_id, stub);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
