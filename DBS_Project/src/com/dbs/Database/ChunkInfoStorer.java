package com.dbs.Database;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


public class ChunkInfoStorer {

    private static ChunkInfoStorer instance = null;

    synchronized public static ChunkInfoStorer getInstance(){
        if(ChunkInfoStorer.instance == null){
            ChunkInfoStorer.instance = new ChunkInfoStorer();
        }

        return ChunkInfoStorer.instance;
    }

    //private ConcurrentHashMap<TaskLogKey, ChunkInfo> tasks = new ConcurrentHashMap<>();

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


    public void save() {
        //SAVE INFO TO HARD DRIVE HERE
    }
}




class ChunkKey{
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