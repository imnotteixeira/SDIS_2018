package com.angelo;

import java.io.IOException;
import java.net.SocketException;

public class Main {

    public static void main(String[] args) {

        int port = 8765;

        try {
            System.out.println("Started Application");
            UDPServer server = new UDPServer(port);
            server.start();



        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
