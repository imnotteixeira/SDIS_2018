package com.angelo;

import com.angelo.Client.SocketClient;
import com.angelo.Server.MulticastSender;
import com.angelo.Server.UDPServer;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {

        String mode = args[0];

        if(mode.equals("server")) {
            initServer(Arrays.copyOfRange(args, 1, args.length));
            return;
        }
        if(mode.equals("client")) {
            initClient(Arrays.copyOfRange(args, 1, args.length));
            return;
        }

        System.out.println("Invalid program arguments! Format: <server|client> <...specific args>");
        return;

    }

    private static void initServer(String[] args) {

        if(args.length != 2) {
            System.out.println("Invalid program arguments! Format: server <port> <timeout>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        int timeout = Integer.parseInt(args[1]);

        MulticastSender multicastSender = null;
        try {
            multicastSender = new MulticastSender("231.0.0.0", port);

            startAnnouncer(multicastSender, port, 1000);
        } catch (SocketException e) {
            e.printStackTrace();
        }


        try {
            System.out.println("Started Server! Creating Socket...");
            UDPServer server = new UDPServer(port, timeout);
            server.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void initClient(String[] args) {

        if(args.length != 3) {
            System.out.println("Invalid program arguments! Format: client <host> <port> <msg>");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        System.out.println("Finding the server....");

        MulticastListener multicastListener = new MulticastListener(host, port);
        DatagramPacket announcement = multicastListener.listen();



        InetAddress communicationHost = announcement.getAddress();
        String announcementContent = new String(announcement.getData(), 0, announcement.getLength());
        int communicationPort = Integer.parseInt(announcementContent);

        final int N_ATTEMPTS = 10;

        String msg = args[2];

        SocketClient client;

        try {
            client = new SocketClient(communicationHost, communicationPort);
        }catch(Exception e){
            System.out.println("ERROR OPENING SOCKET");
            e.printStackTrace();
            return;
        }

        for(int i = 0; i < N_ATTEMPTS; i++) {
            System.out.println("Attempt no. " + (i+1));
            try {
                client.sendMessage(msg);
            }catch(Exception e){
                System.out.println("ERROR SENDING REQUEST");
                e.printStackTrace();
            }
            try{
                System.out.println(client.getResponse());
                break;
            }catch(PortUnreachableException e){
                System.out.println("Unable to establish connection");
            }catch(SocketTimeoutException e){
                System.out.println("Request timed out!");
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }


    private static void startAnnouncer(MulticastSender sender, int port, int interval) {
        ScheduledThreadPoolExecutor threadPoolExecutor = new ScheduledThreadPoolExecutor(1);

        threadPoolExecutor.schedule(() -> sender.send(port),  interval, TimeUnit.MILLISECONDS);
    }
}
