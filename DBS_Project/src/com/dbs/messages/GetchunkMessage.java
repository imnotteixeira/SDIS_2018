package com.dbs.messages;

import java.net.MulticastSocket;

public class GetchunkMessage extends PeerMessage {
    private final String chunkNo;

    public GetchunkMessage(byte[] version, String senderId, String fileId, String chunkNo) {
        super("GETCHUNK", version, senderId, fileId);
        this.chunkNo = chunkNo;
    }

    @Override
    public void send(MulticastSocket socket, String host, int port) {

    }
}
