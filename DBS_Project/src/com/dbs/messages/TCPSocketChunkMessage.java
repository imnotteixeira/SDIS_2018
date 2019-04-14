package com.dbs.messages;

import com.dbs.PeerController;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TCPSocketChunkMessage extends PeerMessage {

    private final String chunkNo;
    private int headerSize;
    private String hostname;
    private int port;

    public TCPSocketChunkMessage(String senderId, String fileId, String chunkNo, String hostname, int port) {
        super("CHUNK", "1.1".getBytes(), senderId, fileId);
        this.chunkNo = chunkNo;
        this.hostname = hostname;
        this.port = port;


        this.headerSize = this.toString().getBytes().length + PeerMessage.CRLF.getBytes().length * 2;
    }

    public static TCPSocketChunkMessage fromString(byte[] src) throws IllegalStateException {


        Pattern r = Pattern.compile("(CHUNK)\\s+(\\d\\.\\d)\\s+(\\d+)\\s+(\\w+)\\s+(\\d{1,6})\\s+"+
                PeerMessage.CRLF +
                "(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+(\\d+)\\s+" +
                PeerMessage.CRLF +
                PeerMessage.CRLF
        );


        Matcher m = r.matcher(new String(src, 0, src.length));
        m.find();

        String senderId = m.group(3);
        String fileId = m.group(4);
        String chunkNo = m.group(5);
        String hostname = m.group(6);
        String port = m.group(7);


        return new TCPSocketChunkMessage(senderId, fileId, chunkNo, hostname, Integer.valueOf(port));
    }

    @Override
    public String toString() {
        return super.toString() + " "
                + chunkNo + " "
                + PeerMessage.CRLF
                + hostname + " "
                + port + " ";
    }

    public byte[] getByteMsg() {
        return ByteBuffer.allocate(this.headerSize)
                .put(toString().getBytes())
                .put(PeerMessage.CRLF.getBytes())
                .put(PeerMessage.CRLF.getBytes()).array();
    }

    @Override
    public void send() {

        String hostname = PeerController.connectionInfo.getRecoveryChannelHostname();
        int port = PeerController.connectionInfo.getRecoveryPort();

        final byte[] msg = getByteMsg();

        try {
            PeerController.getInstance().getConnectionInfo().getRecoveryChannelCommunicator().send(msg, hostname, port);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getHostname() {
        return hostname;
    }

    public int getPort() {
        return port;
    }

    public String getChunkNo() {
        return chunkNo;
    }
}
