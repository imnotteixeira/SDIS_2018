package com.dbs;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    public static void main(String[] args){
        try {
            Registry reg = LocateRegistry.getRegistry("localhost");

            IPeerInterface stub = (IPeerInterface) reg.lookup("peer_id");

            int res = stub.sum(1,2);

            System.out.println(res);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }
}
