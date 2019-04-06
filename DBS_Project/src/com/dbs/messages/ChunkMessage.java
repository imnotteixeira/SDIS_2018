package com.dbs.messages;

public class ChunkMessage extends PeerMessage {
    private final String chunkNo;
    private final byte[] body;

    public ChunkMessage(byte[] version, String senderId, String fileId, String chunkNo, byte[] body) {
        super("CHUNK", version, senderId, fileId);
        this.chunkNo = chunkNo;
        this.body = body;
    }

    @Override
    public void send(String host, int port) {

    }
}
