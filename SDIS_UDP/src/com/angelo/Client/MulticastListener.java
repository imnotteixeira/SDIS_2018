package com.angelo.Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastListener {

    private String hostname;
    private int port;
    private MulticastSocket socket;
    private static final int BUF_SIZE = 512;

    public MulticastListener(String hostname, int port){
        this.hostname = hostname;
        this.port = port;
    }

    public DatagramPacket listen(){
        try {
            byte[] buf = new byte[MulticastListener.BUF_SIZE];

            socket = new MulticastSocket(this.port);
            InetAddress group = InetAddress.getByName(this.hostname);
            socket.joinGroup(group);
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);

            socket.leaveGroup(group);
            socket.close();

            return packet;
        } catch (IOException e) {
            System.out.println("FAILED TO LISTEN TO MULTICAST");
            e.printStackTrace();
        }

        return null;
    }

}
