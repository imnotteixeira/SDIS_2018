package com.dbs;

public class Chunk extends PeerMessage {
    private final String chunkNo;
    private final byte[] body;

    public Chunk(byte[] version, String senderId, String fileId, String chunkNo, byte[] body) {
        super("CHUNK", version, senderId, fileId);
        this.chunkNo = chunkNo;
        this.body = body;
    }
}
