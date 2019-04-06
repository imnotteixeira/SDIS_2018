package com.dbs;

import com.dbs.listeners.Listener;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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

        socket.receive(packet);

        return packet;
    }
}
