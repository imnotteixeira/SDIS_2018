package com.dbs.messages;


import java.net.MulticastSocket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

abstract public class PeerMessage {

    public static String CRLF = "\r\n";

    private String messageType;
    private byte[] version;

    private String senderId;
    private String fileId;

    public PeerMessage(String messageType, byte[] version, String senderId, String fileId) {
        this.messageType = messageType;
        this.version = version;
        this.senderId = senderId;
        this.fileId = fileId;
    }

    public static String getSenderId(byte[] src) throws IllegalStateException {
        Pattern r = Pattern.compile("(\\w+)\\s+(\\d\\.\\d)\\s+(\\d+)\\s+(\\w+)\\s+(\\d{1,6})\\s+");

        Matcher m = r.matcher(new String(src, 0, src.length));

        m.find();


        return m.group(3);
    }

    @Override
    public String toString() {
        return messageType + " "
                + new String(version, 0, version.length) + " "
                + senderId + " "
                + fileId;
    }

    public static String getMessageType(String msg) {
        return msg.substring(0, msg.indexOf(" "));
    }

    abstract public void send(String host, int port);

    public byte[] getVersion() {
        return version;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getFileId() {
        return fileId;
    }
}
