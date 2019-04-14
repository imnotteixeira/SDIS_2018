package com.dbs.messages;

import com.dbs.Communicator;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class ChunkDependentMessage extends PeerMessage{
    protected final String chunkNo;
    protected int headerSize;

    public ChunkDependentMessage(String messageType, byte[] version, String senderId, String fileId, String chunkNo) {
        super(messageType, version, senderId, fileId);
        this.chunkNo = chunkNo;
        this.headerSize = this.toString().getBytes().length + PeerMessage.CRLF.getBytes().length * 2;
    }

    protected abstract String getHostname();
    protected abstract int getPort();
    protected abstract Communicator getCommunicator();

    @Override
    public String toString() {
        return super.toString() + " "
                + chunkNo + " ";
    }

    public byte[] getByteMsg() {
        return ByteBuffer.allocate(this.headerSize)
                .put(toString().getBytes())
                .put(PeerMessage.CRLF.getBytes())
                .put(PeerMessage.CRLF.getBytes()).array();
    }

    @Override
    public void send() {

        String hostname = this.getHostname();
        int port = this.getPort();

        final byte[] msg = getByteMsg();

        try {
            this.getCommunicator().send(msg, hostname, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getChunkNo() {
        return chunkNo;
    }



}
