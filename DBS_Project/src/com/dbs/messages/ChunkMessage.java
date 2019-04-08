package com.dbs.messages;

import com.dbs.PeerController;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChunkMessage extends PeerMessage {
    private final String chunkNo;
    private final byte[] body;
    private int headerSize;

    public ChunkMessage(byte[] version, String senderId, String fileId, String chunkNo, byte[] body) {
        super("CHUNK", version, senderId, fileId);
        this.chunkNo = chunkNo;
        this.body = body;


        this.headerSize = this.toString().getBytes().length + PeerMessage.CRLF.getBytes().length * 2;
    }

    public static ChunkMessage fromString(byte[] src) throws IllegalStateException {


        Pattern r = Pattern.compile("(CHUNK)\\s+(\\d\\.\\d)\\s+(\\d+)\\s+(\\w+)\\s+(\\d{1,6})\\s+" +
                PeerMessage.CRLF +
                PeerMessage.CRLF
        );


        Matcher m = r.matcher(new String(src, 0, src.length));
        m.find();

        byte[] version = m.group(2).getBytes();
        String senderId = m.group(3);
        String fileId = m.group(4);
        String chunkNo = m.group(5);

        int headerSize = m.group(0).getBytes().length;

        byte[] body = Arrays.copyOfRange(src, headerSize, src.length);


        return new ChunkMessage(version, senderId, fileId, chunkNo, body);
    }

    @Override
    public String toString() {
        return super.toString() + " "
                + chunkNo + " ";
    }

    public byte[] getByteMsg() {
        return ByteBuffer.allocate(this.headerSize + body.length)
                .put(toString().getBytes())
                .put(PeerMessage.CRLF.getBytes())
                .put(PeerMessage.CRLF.getBytes())
                .put(body).array();
    }

    @Override
    public void send(String hostname, int port) {
        final byte[] msg = getByteMsg();

        try {
            PeerController.getInstance().getConnectionInfo().getRecoveryChannelCommunicator().send(msg, hostname, port);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
