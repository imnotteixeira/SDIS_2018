package com.dbs.messages;

import com.dbs.Communicator;
import com.dbs.PeerController;

import java.io.IOException;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PutchunkMessage extends ChunkDependentMessage {
    private final byte[] body;
    private final String replicationDegree;

    public PutchunkMessage(byte[] version, String senderId, String fileId, String chunkNo, String replicationDegree, byte[] body) {
        super("PUTCHUNK", version, senderId, fileId, chunkNo);
        this.body = body;
        this.replicationDegree = replicationDegree;

        this.headerSize = this.toString().getBytes().length + PeerMessage.CRLF.getBytes().length * 2;
    }

    public static PutchunkMessage fromString(byte[] src) throws IllegalStateException {


        Pattern r = Pattern.compile("(PUTCHUNK)\\s+(\\d\\.\\d)\\s+(\\d+)\\s+(\\w+)\\s+(\\d{1,6})\\s+(\\d)\\s+" +
                PeerMessage.CRLF +
                PeerMessage.CRLF
                );


        Matcher m = r.matcher(new String(src, 0, src.length));
        m.find();

        byte[] version = m.group(2).getBytes();
        String senderId = m.group(3);
        String fileId = m.group(4);
        String chunkNo = m.group(5);
        String replicationDegree = m.group(6);

        int headerSize = m.group(0).getBytes().length;

        byte[] body = Arrays.copyOfRange(src, headerSize, src.length);


        return new PutchunkMessage(version, senderId, fileId, chunkNo, replicationDegree, body);
    }

    @Override
    protected String getHostname() { return PeerController.connectionInfo.getBackupChannelHostname(); }

    @Override
    protected int getPort() { return PeerController.connectionInfo.getBackupPort(); }

    @Override
    protected Communicator getCommunicator() { return PeerController.connectionInfo.getControlChannelCommunicator(); }

    @Override
    public String toString() {
        return super.toString()
                + replicationDegree + " ";
    }

    public byte[] getByteMsg() {
        return ByteBuffer.allocate(this.headerSize + body.length)
                .put(toString().getBytes())
                .put(PeerMessage.CRLF.getBytes())
                .put(PeerMessage.CRLF.getBytes())
                .put(body).array();
    }

    public byte[] getBody() {
        return body;
    }

    public String getReplicationDegree() {
        return replicationDegree;
    }
}
