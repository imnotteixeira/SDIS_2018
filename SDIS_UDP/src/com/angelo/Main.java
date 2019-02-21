package com.angelo;

import com.angelo.Client.SocketClient;
import com.angelo.Server.UDPServer;

import java.io.IOException;
import java.net.PortUnreachableException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

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

        final int N_ATTEMPTS = 10;


        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String msg = args[2];

        SocketClient client;

        try {
            client = new SocketClient(host, port);
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
}
