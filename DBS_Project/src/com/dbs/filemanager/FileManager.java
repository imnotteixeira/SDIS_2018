package com.dbs.filemanager;

import com.dbs.utils.ByteToHex;

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

    public void saveFile(String dir, String fileId, byte[] fileData, int chunkSize) {

        ArrayList<byte[]> chunks = (ArrayList<byte[]>) FileManager.splitFile(fileData, chunkSize);

        for (int i = 0; i < chunks.size(); i++) {
            Path path = Paths.get(dir, fileId ,String.valueOf(i));
            if(Files.notExists(path.getParent())) {
                try {
                    Files.createDirectories(path.getParent());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            writeChunk(path, chunks.get(i));
        }
    }

    public static void storeChunk(String dir, String fileId, String chunkNo, byte[] chunkBody) {


            Path path = Paths.get(dir, fileId, chunkNo);
            if(Files.notExists(path.getParent())) {
                try {
                    Files.createDirectories(path.getParent());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            writeChunk(path, chunkBody);
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

    public static byte[] getChunk(String dir, String fileId, int chunkNo) throws FileNotFoundException {
        Path path = Paths.get(dir, fileId , String.valueOf(chunkNo));

        byte[] data = new byte[0];

        if(Files.notExists(path)) {
            throw new FileNotFoundException();
        } else {
            try {
                data = new String(Files.readAllBytes(path)).getBytes();
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
}

