package com.dbs;

import java.rmi.*;

public interface IPeerInterface extends Remote {
    void backup(String filePath, int replicationDegree) throws RemoteException;
    void recover(String filePath) throws RemoteException;
}
