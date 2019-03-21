package com.dbs.messages;

public class GetchunkMessage extends PeerMessage {
    private final String chunkNo;

    public GetchunkMessage(byte[] version, String senderId, String fileId, String chunkNo) {
        super("GETCHUNK", version, senderId, fileId);
        this.chunkNo = chunkNo;
    }
}
