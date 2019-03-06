package com.dbs;

import java.rmi.*;

public interface IPeerInterface extends Remote {
    int sum(int a, int b) throws RemoteException;
}
