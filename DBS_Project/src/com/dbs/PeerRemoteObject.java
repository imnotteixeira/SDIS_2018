package com.dbs;

import com.dbs.filemanager.FileManager;
import com.dbs.utils.ByteToHex;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
