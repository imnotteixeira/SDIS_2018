package com.angelo.Server;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class MulticastSender {

    String hostname;
    int port;
    private DatagramSocket datagramSocket;
    private static final int TIMEOUT = 10000;

    MulticastSender(String hostname, int port) throws SocketException {
        this.hostname = hostname;
        this.port = port;
        datagramSocket = new DatagramSocket();
        datagramSocket.setSoTimeout(MulticastSender.TIMEOUT);
    }

    void send(Integer listeningPort){
        byte[] buf = listeningPort.toString().getBytes();

        try {
            DatagramPacket packet = new DatagramPacket(buf, buf.length,
                    InetAddress.getByName(this.hostname), this.port);
            this.datagramSocket.send(packet);
        }catch(Exception e){
            System.out.println("--- ERROR SENDING MULTICAST ---");
            e.printStackTrace();
        }
    }

}
