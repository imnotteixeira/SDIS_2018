package com.dbs;

import com.dbs.listeners.Listener;
import com.dbs.messages.PeerMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class Communicator {
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
        byte[] buf = new byte[Listener.BUF_SIZE];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        while(true) {
            socket.receive(packet);
            String senderId = PeerMessage.getSenderId(Arrays.copyOf(packet.getData(), packet.getLength()));
            if(!senderId.equals(Peer.PEER_ID)) {
                break;
            }
        }


        return packet;
    }
}
