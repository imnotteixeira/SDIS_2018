package com.dbs;

import com.dbs.filemanager.FileManager;

import java.rmi.RemoteException;

public class PeerRemoteObject implements IPeerInterface {

    private final String dir;
    private PeerController peer;

    public PeerRemoteObject(PeerController peer) {

        this.peer = peer;
        this.dir = FileManager.BACKUP_DIR;
    }

    @Override
    public void backup(String filePath, int replicationDegree) throws RemoteException {

        peer.backup(filePath, replicationDegree);

    }

    @Override
    public String backup_enhanced(String filePath, int replicationDegree) throws RemoteException {

        if(peer.isCompatible("1.1")) {
            peer.backup(filePath, replicationDegree);
            return "";
        }

        return "Peer is not compatible with the requested protocol, use the base version instead!";





    }

    @Override
    public void recover(String filePath) throws RemoteException {
        peer.recover(filePath);

    }

    @Override
    public String recover_enhanced(String filePath) throws RemoteException {

        if(peer.isCompatible("1.1")) {
            peer.recover_enhanced(filePath);
            return "";
        }

        return "Peer is not compatible with the requested protocol, use the base version instead!";

    }

    @Override
    public void delete(String filePath) throws RemoteException {
        peer.delete(filePath);
    }

    @Override
    public void reallocateSpace(int newSizeKB) throws RemoteException {
        peer.reallocateSpace(newSizeKB);
    }

    @Override
    public String getState(){
        return peer.getState();
    }
}
