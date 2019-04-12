package com.dbs;

import java.rmi.*;

public interface IPeerInterface extends Remote {
    void backup(String filePath, int replicationDegree) throws RemoteException;
    void recover(String filePath) throws RemoteException;
    void recover_enhanced(String filePath) throws RemoteException;
    void delete(String filePath) throws RemoteException;
    void reallocateSpace(int newSizeKB) throws RemoteException;
}
