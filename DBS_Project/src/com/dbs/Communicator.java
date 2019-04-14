package com.dbs;

import com.dbs.messages.PeerMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class Communicator {

    public static final int BUF_SIZE = 65622;
    private DatagramSocket socket;

    public Communicator(DatagramSocket socket) {
        this.socket = socket;
    }

    public synchronized void send(byte[] msg, String hostname, int port) throws IOException{

        DatagramPacket packet = new DatagramPacket(msg, msg.length,
                InetAddress.getByName(hostname), port);

        socket.send(packet);
    }

    public DatagramPacket receive() throws IOException{
        byte[] buf = new byte[BUF_SIZE];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        while(true) {

            socket.receive(packet);

            byte[] msgSrc = Arrays.copyOf(packet.getData(), packet.getLength());

            String senderId = PeerMessage.getSenderId(msgSrc);

            String version = PeerMessage.getMsgProtocolVersion(msgSrc);

            if(!senderId.equals(Peer.PEER_ID) && PeerController.getInstance().isCompatible(version)) {
                return packet;
            }
        }
    }
}
