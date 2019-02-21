package com.angelo;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPServer {

    private DatagramSocket socket;
    private final static int BUF_SIZE = 512;
    private final static int TIMEOUT_MS = 20000;
    private final static String MSG_FORMAT = "/";

    private PlateManager plateManager;

    public UDPServer(int port) throws SocketException {
        this.socket = new DatagramSocket(port);
        this.socket.setSoTimeout(UDPServer.TIMEOUT_MS);

        this.plateManager = new PlateManager();

        System.out.println("Started Server on Port " + port);

    }

    public void start() throws IOException {
        boolean stop = false;

        while(!stop) {

            byte[] buf = new byte[UDPServer.BUF_SIZE];
            DatagramPacket rcvd_packet = new DatagramPacket(buf, UDPServer.BUF_SIZE);

            this.socket.receive(rcvd_packet);


            byte[] raw_msg = rcvd_packet.getData();
            int raw_msg_size = rcvd_packet.getLength();

            String msg = new String(raw_msg, 0, raw_msg_size);

            processMessage(msg, rcvd_packet);

        }
    }

    private void processMessage(String msg, DatagramPacket packet) throws IOException {

        String[] args = msg.split(UDPServer.MSG_FORMAT);

        if(args[0].equals("register")) {
            parseRegisterRequest(args, packet);
        } else if(args[0].equals("lookup")) {
            parseLookupRequest(args, packet);
        } else {
            sendError(args, packet);
        }
    }

    private void parseRegisterRequest(String [] args, DatagramPacket packet) throws IOException {
        if(args.length != 3) {
            sendError(args, packet);
            return;
        }
//        String response = "SO you want me to register stuff, " + args[1] + " : " + args[2];

        String response = this.plateManager.register(args[1], args[2]);

        packet.setData(response.getBytes());
        this.socket.send(packet);
    }

    private void parseLookupRequest(String [] args, DatagramPacket packet) throws IOException {
        if(args.length != 2) {
            sendError(args, packet);
            return;
        }
//        String response = "SO you want me to lookup stuff, " + args[1];
        String response = this.plateManager.lookup(args[1]);

        packet.setData(response.getBytes());
        this.socket.send(packet);
    }

    private void sendError(String [] args, DatagramPacket packet) throws IOException {
        String response = "SO you are trying to fool me";

        packet.setData(response.getBytes());
        this.socket.send(packet);
    }
}
