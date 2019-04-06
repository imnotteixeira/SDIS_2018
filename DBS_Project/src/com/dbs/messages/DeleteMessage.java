package com.dbs.messages;

public class DeleteMessage extends PeerMessage {
    public DeleteMessage(byte[] version, String senderId, String fileId) {
        super("DELETE", version, senderId, fileId);
    }

    @Override
    public void send(String host, int port) {

    }
}
