package com.dbs;

import java.rmi.*;

public interface IPeerInterface extends Remote {
    void backup(String filePath, int replicationDegree) throws RemoteException;
    void recover(String filePath) throws RemoteException;
    void delete(String filePath) throws RemoteException;
    void removed(String filePath, int chunkNo) throws RemoteException;
}
