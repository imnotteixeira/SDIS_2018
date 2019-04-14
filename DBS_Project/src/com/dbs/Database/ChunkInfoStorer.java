package com.dbs.Database;

import com.dbs.Peer;
import com.dbs.PeerController;
import com.dbs.filemanager.FileManager;
import com.dbs.utils.Logger;

import java.io.*;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


public class ChunkInfoStorer implements Serializable {

    private static ChunkInfoStorer instance = null;
    private int storageSizeInBytes = 0;

    private ConcurrentHashMap<ChunkKey, ChunkInfo> chunkInfos = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, String> backedUpPathnameToFileId = new ConcurrentHashMap<>();

    public static ChunkInfoStorer loadFromFile(){
        try {
            ObjectInputStream i = new ObjectInputStream(new FileInputStream(new File(FileManager.PEER_DIR,"state.pst")));
            // Write objects to file
            ChunkInfoStorer.instance = (ChunkInfoStorer) i.readObject();

            i.close();

            return instance;
        } catch (Exception e) {
           Logger.log("Could not load state from file");
        }

        return getInstance();
    }

    synchronized public static ChunkInfoStorer getInstance(){
        if(ChunkInfoStorer.instance == null){
            ChunkInfoStorer.instance = new ChunkInfoStorer();
        }

        return ChunkInfoStorer.instance;
    }

    public ChunkInfo getChunkInfo(String fileId, String chunkNo) {
        return getChunkInfo(fileId, Integer.parseInt(chunkNo));
    }

    public ChunkInfo getChunkInfo(String fileId, int chunkNo){
        ChunkKey key = new ChunkKey(fileId, chunkNo);

        if(!chunkInfos.containsKey(key)){
            chunkInfos.put(key,
                    new ChunkInfo()
            );
        }

        return chunkInfos.get(key);
    }


    public static void save() {
        ChunkInfoStorer object = ChunkInfoStorer.getInstance();

        try {
            ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(new File(FileManager.PEER_DIR,"state.pst")));
            // Write objects to file
            o.writeObject(object);

            o.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<ChunkKey> getChunksToRemoveForNewSpace(int allocatedSpaceKB) {
        int spaceDiff = this.storageSizeInBytes - (allocatedSpaceKB * 1000);

        System.out.println(spaceDiff);
        if(spaceDiff <= 0){
            return new ArrayList<>();
        }

        Logger.log("[SPACE ALLOCATION] Need to remove " + spaceDiff + " to fit to new allocated space");

        ArrayList<ChunkKey> result = new ArrayList<>();

        for(ChunkKey key : this.chunkInfos.keySet()){
            if(this.chunkInfos.get(key).isStored()) {
                result.add(key);
                spaceDiff -= this.chunkInfos.get(key).getBodySize();
                if (spaceDiff <= 0) {
                    break;
                }
            }
        }

        if(spaceDiff > 0){
            Logger.log("Could not reallocate to desired size!");
        }

        for(ChunkKey key : this.chunkInfos.keySet()){
            if(this.chunkInfos.get(key).isStored()) {
                System.out.println(this.chunkInfos.get(key));
            }
        }

        return result;
    }

    public void updateStorageSize(int size) {

        this.storageSizeInBytes += size;
    }

    public int getUsedBytes() {
        return this.storageSizeInBytes;
    }

    public void addPathToFileId(String path, String fileId) {
        this.backedUpPathnameToFileId.put(path, fileId);
    }

    public String getInfo(){

        String result = "--------------------------------------" +
                "\n            BACKED UP FILES" +
                "\n--------------------------------------";

        if(this.backedUpPathnameToFileId.isEmpty()){
            result += "\n No chunks backed up from this peer!";
        }else {
            for (String filePath : this.backedUpPathnameToFileId.keySet()) {
                String fileId = this.backedUpPathnameToFileId.get(filePath);

                result += "\n\nFile Path: " + filePath
                        + "\nFile Id: " + fileId
                        + "\nReplication factor: " + this.chunkInfos.get(new ChunkKey(fileId, 0)).getDesiredReplication()
                        + "\nChunks Information: " + getFileChunkInfo(fileId);
            }
        }

        result += "\n\n\n--------------------------------------" +
                "\n            STORED CHUNKS" +
                "\n--------------------------------------\n";

        int storedChunksCount = 0;

        for (ChunkKey key : this.chunkInfos.keySet()) {
            if (chunkInfos.get(key).isStored()) {
                result += "\n    Chunk" + key.chunkNo + " of File Id " + key.fileId + " - " + chunkInfos.get(key).toString();
                storedChunksCount++;
            }
        }

        if(storedChunksCount == 0) result += "\n No chunks stored in this peer!";

        return result;
    }

    private String getFileChunkInfo(String fileId) {

        String result = "";
        int chunkNo = 0;

        while (this.chunkInfos.containsKey(new ChunkKey(fileId, chunkNo))){
            ChunkInfo info = this.chunkInfos.get(new ChunkKey(fileId, chunkNo));
            result += "\n    Chunk " + chunkNo + " - Perceived Replication Degree: " + info.getPerceivedReplication();
            chunkNo++;
        }

        return result;
    }


}