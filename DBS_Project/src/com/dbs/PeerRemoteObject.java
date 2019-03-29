package com.dbs;

import com.dbs.filemanager.FileManager;
import java.rmi.RemoteException;

public class PeerRemoteObject implements IPeerInterface {

    private final FileManager fm;
    private final String dir;
    private PeerController peer;

    public PeerRemoteObject(PeerController peer, FileManager fm) {
        this.fm = fm;
        this.peer = peer;
        this.dir = peer.getBackupDir();
    }

    @Override
    public void backup(String filePath, int replicationDegree) throws RemoteException {


        peer.backup(filePath, replicationDegree);

    }
}
