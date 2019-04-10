package com.dbs.Database;

import com.dbs.Peer;
import com.dbs.filemanager.FileManager;
import com.dbs.utils.Logger;

import java.io.*;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


public class ChunkInfoStorer implements Serializable {

    private static ChunkInfoStorer instance = null;

    public static ChunkInfoStorer loadFromFile(){
        try {
            ObjectInputStream i = new ObjectInputStream(new FileInputStream(new File(FileManager.PEER_DIR,"state.txt")));
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
            ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(new File(FileManager.PEER_DIR,"state.txt")));
            // Write objects to file
            o.writeObject(object);

            o.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}




class ChunkKey implements Serializable{
    public String fileId;
    public int chunkNo;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChunkKey)) return false;
        ChunkKey chunkKey = (ChunkKey) o;
        return chunkNo == chunkKey.chunkNo &&
                Objects.equals(fileId, chunkKey.fileId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileId, chunkNo);
    }

    ChunkKey(String fileId, int chunkNo){
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }
}