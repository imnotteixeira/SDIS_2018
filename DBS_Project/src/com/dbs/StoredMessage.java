package com.dbs;

public class StoredMessage extends PeerMessage {
    private final String chunkNo;

    public StoredMessage(byte[] version, String senderId, String fileId, String chunkNo) {
        super("STORED", version, senderId, fileId);
        this.chunkNo = chunkNo;
    }
}
