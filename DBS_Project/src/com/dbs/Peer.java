package com.dbs;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Peer {

    public static void main(String[] args) {
        System.out.println("Hello World!");

        String a = "PUTCHUNK 1.0 1 22 333 4 \r\n\r\nbodybodybodybodybody";

        PutchunkMessage p = PutchunkMessage.fromString(a);

        System.out.println(p);

        PeerRemoteObject peer = new PeerRemoteObject();
        IPeerInterface stub = null;

        try {
            stub = (IPeerInterface) UnicastRemoteObject.exportObject(peer, 0);

            try {
                Registry reg = LocateRegistry.getRegistry(1099);

                reg.bind("peer_id", stub);

            } catch (RemoteException e) {
                try {
                    Registry reg = LocateRegistry.createRegistry(1099);

                    reg.bind("peer_id", stub);
                } catch (RemoteException | AlreadyBoundException e1) {
                    e1.printStackTrace();
                }
            } catch (AlreadyBoundException e) {
                e.printStackTrace();
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }





    }
}
