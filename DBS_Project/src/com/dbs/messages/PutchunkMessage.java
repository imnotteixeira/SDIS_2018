package com.dbs.messages;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PutchunkMessage extends PeerMessage {
    private final byte[] body;
    private int headerSize;
    private final String replicationDegree;
    private String chunkNo;

    public PutchunkMessage(byte[] version, String senderId, String fileId, String chunkNo, String replicationDegree, byte[] body) {
        super("PUTCHUNK", version, senderId, fileId);
        this.chunkNo = chunkNo;
        this.body = body;
        this.replicationDegree = replicationDegree;

        this.headerSize = this.toString().getBytes().length + PeerMessage.CRLF.getBytes().length * 2;
    }

    public static PutchunkMessage fromString(byte[] src) {


        Pattern r = Pattern.compile("(PUTCHUNK)\\s(\\d\\.\\d)\\s(\\d+)\\s(\\w+)\\s(\\d{1,6})\\s(\\d)\\s" +
                PeerMessage.CRLF +
                PeerMessage.CRLF
                );


        Matcher m = r.matcher(new String(src, 0, src.length));
        m.find();

        String test = m.group(0);

        byte[] version = m.group(2).getBytes();
        String senderId = m.group(3);
        String fileId = m.group(4);
        String chunkNo = m.group(5);
        String replicationDegree = m.group(6);

        int headerSize = m.group(0).getBytes().length;

        byte[] body = Arrays.copyOfRange(src, headerSize, src.length - 1);


        return new PutchunkMessage(version, senderId, fileId, chunkNo, replicationDegree, body);
    }

    @Override
    public String toString() {
        return super.toString() + " "
                + chunkNo + " "
                + replicationDegree + " ";
    }

    public byte[] getByteMsg() {
        return ByteBuffer.allocate(this.headerSize + body.length)
                .put(toString().getBytes())
                .put(PeerMessage.CRLF.getBytes())
                .put(PeerMessage.CRLF.getBytes())
                .put(body).array();
    }



    @Override
    public void send(MulticastSocket socket, String hostname, int port) {



        final byte[] msg = getByteMsg();

        try {
        DatagramPacket packet = new DatagramPacket(msg, msg.length,
                    InetAddress.getByName(hostname), port);

            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
