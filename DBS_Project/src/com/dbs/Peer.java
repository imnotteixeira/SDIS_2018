package com.dbs;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Peer {

    public static void main(String[] args) {
        System.out.println("Hello World!");


        PeerRemoteObject peer = new PeerRemoteObject();
        try {
            IPeerInterface stub = (IPeerInterface) UnicastRemoteObject.exportObject(peer, 0);

            Registry reg = LocateRegistry.createRegistry(1099);

            reg.bind("peer_id", stub);

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
        }

    }
}
