package com.dbs.Database;

import com.dbs.Peer;
import com.dbs.PeerController;
import com.dbs.filemanager.FileManager;
import com.dbs.utils.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


public class ChunkInfoStorer implements Serializable {

    private static ChunkInfoStorer instance = null;
    private int storageSizeInBytes = 0;

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

    private ConcurrentHashMap<ChunkKey, ChunkInfo> chunkInfos = new ConcurrentHashMap<>();


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

        System.out.println("CHUNKINFOS");
        for(ChunkKey key : this.chunkInfos.keySet()){
            if(this.chunkInfos.get(key).isStored()) {
                System.out.println(this.chunkInfos.get(key));
            }
        }

        return result;
    }

    public void updateStorageSize(int chunksAdded) {
        this.storageSizeInBytes += chunksAdded * PeerController.getInstance().CHUNK_SIZE;
    }

    public int getUsedBytes() {
        return this.storageSizeInBytes;
    }
}