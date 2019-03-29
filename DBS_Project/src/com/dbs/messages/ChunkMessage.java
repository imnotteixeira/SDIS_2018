package com.dbs.messages;

import java.net.MulticastSocket;

public class ChunkMessage extends PeerMessage {
    private final String chunkNo;
    private final byte[] body;

    public ChunkMessage(byte[] version, String senderId, String fileId, String chunkNo, byte[] body) {
        super("CHUNK", version, senderId, fileId);
        this.chunkNo = chunkNo;
        this.body = body;
    }

    @Override
    public void send(MulticastSocket socket, String host, int port) {

    }
}
