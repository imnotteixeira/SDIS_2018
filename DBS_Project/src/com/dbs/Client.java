package com.dbs;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.TimeUnit;

public class Client {

    public static void main(String[] args){
        try {
            Registry reg = LocateRegistry.getRegistry("localhost");

            IPeerInterface stub = (IPeerInterface) reg.lookup("peer_1");

            stub.backup("./8ktest.jpg",2);

            TimeUnit.SECONDS.sleep(10);

            stub.recover("./8ktest.jpg");

            //TimeUnit.SECONDS.sleep(5);

            //stub.delete("./8ktest.jpg");

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
