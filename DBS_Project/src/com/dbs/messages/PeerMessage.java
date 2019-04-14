package com.dbs.messages;

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

    private static String getMsgNthField(byte[] src, int n) {

        Pattern r = Pattern.compile("^(\\w+)\\s+(\\d\\.\\d)\\s+(\\d+)\\s+(.+)\\s+");

        String srcStr = new String(src, 0, src.length);

        Matcher m = r.matcher(srcStr);

        m.find();

        String result = m.group(n);

        return result;
    }

    public static String getSenderId(byte[] src) throws IllegalStateException {
        return getMsgNthField(src, 3);
    }

    public static String getMsgProtocolVersion(byte[] src){
        return getMsgNthField(src, 2);
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

    abstract public void send();

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
