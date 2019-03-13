package com.dbs;

public class DeleteMessage extends PeerMessage {
    public DeleteMessage(byte[] version, String senderId, String fileId) {
        super("DELETE", version, senderId, fileId);
    }
}
