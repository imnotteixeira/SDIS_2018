package com.dbs;

public class PutchunkMessage extends PeerMessage {
    private final byte[] body;
    private String chunkNo;

    public PutchunkMessage(byte[] version, String senderId, String fileId, String chunkNo, byte[] body) {
        super("PUTCHUNK", version, senderId, fileId);
        this.chunkNo = chunkNo;
        this.body = body;
    }

}
