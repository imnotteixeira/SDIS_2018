package com.dbs;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PutchunkMessage extends PeerMessage {
    private final byte[] body;
    private final String replicationDegree;
    private String chunkNo;

    public PutchunkMessage(byte[] version, String senderId, String fileId, String chunkNo, String replicationDegree, byte[] body) {
        super("PUTCHUNK", version, senderId, fileId);
        this.chunkNo = chunkNo;
        this.body = body;
        this.replicationDegree = replicationDegree;
    }

    public static PutchunkMessage fromString(String src) {
        Pattern r = Pattern.compile("(PUTCHUNK)\\s(\\d.\\d)\\s(\\d+)\\s(\\w+)\\s(\\d{1,6})\\s(\\d)\\s" +
                PeerMessage.CRLF +
                PeerMessage.CRLF +
                "(.*)", Pattern.DOTALL);

        Matcher m = r.matcher(src);
        m.find();
        byte[] version = m.group(2).getBytes();
        String senderId = m.group(3);
        String fileId = m.group(4);
        String chunkNo = m.group(5);
        String replicationDegree = m.group(6);
        byte[] body = m.group(7).getBytes();


        return new PutchunkMessage(version, senderId, fileId, chunkNo, replicationDegree, body);
    }

    @Override
    public String toString() {
        return super.toString()+
            "body=" + new String(body) +
            ", replicationFactor='" + replicationDegree+ '\'' +
            ", chunkNo='" + chunkNo + '\'' +
            '}';
    }
}
