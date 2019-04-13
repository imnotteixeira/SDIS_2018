package com.dbs.filemanager;

import com.dbs.Peer;
import com.dbs.PeerController;
import com.dbs.utils.ByteToHex;
import com.dbs.utils.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileManager {

    public static String PEER_DIR = null;
    public static String BACKUP_DIR = null;
    public static String RESTORED_DIR = null;

    public static void removeRestoredFile(String fileName) {
        File chunkFile = Paths.get(RESTORED_DIR, fileName).toFile();
        if(chunkFile.exists()) {
            chunkFile.delete();
        }
    }

    public void saveFile(String dir, String fileId, byte[] fileData, int chunkSize) {

        ArrayList<byte[]> chunks = (ArrayList<byte[]>) FileManager.splitFile(fileData, chunkSize);

        for (int i = 0; i < chunks.size(); i++) {
            Path path = Paths.get(dir, fileId ,String.valueOf(i));
            if(Files.notExists(path.getParent())) {
                try{
                    Files.createDirectories(path.getParent());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            writeChunk(path, chunks.get(i));
        }
    }

    public static void storeChunk(String fileId, String chunkNo, byte[] chunkBody) {

        Path chunkPath = Paths.get(BACKUP_DIR, fileId, "chk" + chunkNo);

        if(Files.notExists(chunkPath)) {
            try {
                Files.createDirectories(chunkPath.getParent());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        writeChunk(chunkPath, chunkBody);
    }

    public static void removeChunk(String fileId, int chunkNo) {

        File chunkFile = Paths.get(BACKUP_DIR, fileId, "chk" + chunkNo).toFile();

        if(chunkFile.exists()) {
            chunkFile.delete();
        }

    }

    public static void createPeerFileTree(){

        if(FileManager.PEER_DIR == null){
            FileManager.PEER_DIR = "peer_"+ Peer.PEER_ID;
        }

        Path backupPath = Paths.get(PEER_DIR, "backup");
        BACKUP_DIR = backupPath.toString();

        Path restoredPath = Paths.get(PEER_DIR, "restored");
        RESTORED_DIR = restoredPath.toString();

        if(Files.notExists(backupPath)) {
            try {
                Files.createDirectories(backupPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(Files.notExists(restoredPath)) {
            try {
                Files.createDirectories(restoredPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public static void writeChunk(Path chunkPath, byte[] data) {

        try {
            OutputStream out = new BufferedOutputStream(
                    Files.newOutputStream(
                            chunkPath,
                            StandardOpenOption.WRITE,
                            StandardOpenOption.CREATE
                    )
            );

            out.write(data, 0, data.length);
            out.close();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static List<byte[]> splitFile(byte[] source, int chunkSize) {

        List<byte[]> result = new ArrayList<byte[]>();
        int start = 0;
        while (start < source.length) {
            int end = Math.min(source.length, start + chunkSize);
            result.add(Arrays.copyOfRange(source, start, end));
            start += chunkSize;
        }

        //The file size is multiple of chunkSize
        if(start == source.length) {
            result.add(new byte[0]);
        }

        return result;
    }

    public static byte[] getChunk(String fileId, int chunkNo) throws FileNotFoundException {
        Path path = Paths.get(BACKUP_DIR, fileId, "chk" + chunkNo);

        byte[] data = new byte[0];

        if(Files.notExists(path)) {
            throw new FileNotFoundException();
        } else {
            try {
                data = Files.readAllBytes(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public static String calcFileId(Path path) throws IOException {
        BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);

        String fileId_raw = path.getFileName().toString() + attr.creationTime().toString();
        String fileId = "";
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");

            byte[] fileId_hash = digest.digest(fileId_raw.getBytes(StandardCharsets.UTF_8));

            fileId = ByteToHex.convert(fileId_hash);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return fileId;
    }



    public static void appendChunkToFile(String fileName, byte[] body){
        try {
            OutputStream out = new BufferedOutputStream(
                    Files.newOutputStream(
                            Paths.get(RESTORED_DIR, fileName),
                            StandardOpenOption.WRITE,
                            StandardOpenOption.APPEND
                    )
            );

            out.write(body, 0, body.length);
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void resetRecoveredFileIfExists(String fileName){
        try {
            BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(Paths.get(RESTORED_DIR, fileName)));
            out.write(new byte[0]);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static boolean deleteBackupFolder(String fileId){
        File directory = Paths.get(BACKUP_DIR, fileId).toFile();

        if(!directory.exists()) {
            return false;
        }

        for(File chunkFile : directory.listFiles()) {
            chunkFile.delete();
        }

        directory.delete();

        return true;
    }

}

