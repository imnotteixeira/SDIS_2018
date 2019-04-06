package com.dbs.messages;

public class RemovedMessage extends PeerMessage {
    private final String chunkNo;

    public RemovedMessage(byte[] version, String senderId, String fileId, String chunkNo) {
        super("REMOVED", version, senderId, fileId);
        this.chunkNo = chunkNo;
    }

    @Override
    public void send(String host, int port) {

    }
}
