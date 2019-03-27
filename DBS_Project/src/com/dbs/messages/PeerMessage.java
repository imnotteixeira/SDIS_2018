package com.dbs.messages;


import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

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

    @Override
    public String toString() {
        return "PeerMessage{" +
                "messageType='" + messageType + '\'' +
                ", version=" + Arrays.toString(version) +
                ", senderId='" + senderId + '\'' +
                ", fileId='" + fileId + '\'' +
                '}';
    }

    abstract public void send(MulticastSocket socket);
}
