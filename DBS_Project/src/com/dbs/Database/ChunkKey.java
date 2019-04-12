package com.dbs.Database;

import java.io.Serializable;
import java.util.Objects;

public class ChunkKey implements Serializable {
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

    @Override
    public String toString() {
        return "ChunkKey{" +
                "fileId='" + fileId + '\'' +
                ", chunkNo=" + chunkNo +
                '}';
    }
}