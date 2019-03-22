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
import java.util.Base64;

public class PeerRemoteObject implements IPeerInterface {

    private final FileManager fm;
    private final String dir;

    public PeerRemoteObject(FileManager fm, String dir) {
        this.fm = fm;
        this.dir = dir;
    }

    @Override
    public void backup(String filePath, int replicationDegree) throws RemoteException {



        try {
            Path path = Paths.get(filePath);
            byte[] data = Files.readAllBytes(path);

            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);

            String fileId_raw = path.getFileName().toString() + attr.creationTime().toString();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fileId_hash = digest.digest(fileId_raw.getBytes(StandardCharsets.UTF_8));

//            String fileId_encoded = Base64.getEncoder().encodeToString(fileId_hash);
//
//            String fileId = fileId_encoded.replace('/', '_');
            String fileId = ByteToHex.convert(fileId_hash);


            System.out.println("FileName: " + path.getFileName().toString());
            System.out.println("CreationTime: " + attr.creationTime());

            this.fm.saveFile(this.dir, fileId, data, 5);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }
}
