package com.dbs;



public class PeerMessage {

    private String messageType;
    private byte[] version;

    private String senderId;
    private String fileId;

    public PeerMessage(String messageType, byte[] version, String senderId, String fileId) {
        this.messageType = messageType;
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
    }
}
