package com.dbs.messages;

import java.net.MulticastSocket;

public class StoredMessage extends PeerMessage {
    private final String chunkNo;

    public StoredMessage(byte[] version, String senderId, String fileId, String chunkNo) {
        super("STORED", version, senderId, fileId);
        this.chunkNo = chunkNo;
    }

    @Override
    public void send(MulticastSocket socket, String host, int port) {

    }
}
