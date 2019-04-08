package com.dbs.messages;

import com.dbs.PeerController;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StoredMessage extends PeerMessage {
    private final String chunkNo;
    private int headerSize;

    public StoredMessage(byte[] version, String senderId, String fileId, String chunkNo) {
        super("STORED", version, senderId, fileId);
        this.chunkNo = chunkNo;

        this.headerSize = this.toString().getBytes().length + PeerMessage.CRLF.getBytes().length * 2;
    }

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
    public void send(String hostname, int port) {
        final byte[] msg = getByteMsg();

        try {
            PeerController.getInstance().getConnectionInfo().getBackupChannelCommunicator().send(msg, hostname, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static StoredMessage fromString(byte[] src) throws IllegalStateException {

        Pattern r = Pattern.compile("(STORED)\\s+(\\d\\.\\d)\\s+(\\d+)\\s+(\\w+)\\s+(\\d{1,6})\\s+" +
                PeerMessage.CRLF +
                PeerMessage.CRLF
        );

        Matcher m = r.matcher(new String(src, 0, src.length));
        m.find();

        byte[] version = m.group(2).getBytes();
        String senderId = m.group(3);
        String fileId = m.group(4);
        String chunkNo = m.group(5);


        return new StoredMessage(version, senderId, fileId, chunkNo);
    }

    public String getChunkNo() {
        return chunkNo;
    }
}
