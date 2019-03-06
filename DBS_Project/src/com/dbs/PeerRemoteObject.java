package com.dbs;

import java.rmi.RemoteException;

public class PeerRemoteObject implements IPeerInterface {

    @Override
    public int sum(int a, int b) throws RemoteException {
        return a+b;
    }
}
