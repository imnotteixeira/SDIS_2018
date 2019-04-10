package com.dbs;

import com.dbs.filemanager.FileManager;

import java.io.File;
import java.rmi.RemoteException;

public class PeerRemoteObject implements IPeerInterface {

    private final FileManager fm;
    private final String dir;
    private PeerController peer;

    public PeerRemoteObject(PeerController peer, FileManager fm) {
        this.fm = fm;
        this.peer = peer;
        this.dir = FileManager.BACKUP_DIR;
    }

    @Override
    public void backup(String filePath, int replicationDegree) throws RemoteException {

        peer.backup(filePath, replicationDegree);

    }

    @Override
    public void recover(String filePath) throws RemoteException {
        peer.recover(filePath);

    }

    @Override
    public void delete(String filePath) throws RemoteException {
        peer.delete(filePath);
    }

    @Override
    public void removed(String filePath, int chunkNo) throws RemoteException {
        peer.removed(filePath, chunkNo);
    }
}
