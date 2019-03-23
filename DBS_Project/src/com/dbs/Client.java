package com.dbs;

import java.io.IOException;
import java.net.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    public static void main(String[] args){
        try {
            Registry reg = LocateRegistry.getRegistry("localhost");

            IPeerInterface stub = (IPeerInterface) reg.lookup("peer_1");

            stub.backup("./testfile.txt",2);

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

        DatagramSocket socket;
        InetAddress group;
        byte[] buf;


        try {
            socket = new DatagramSocket();

        group = InetAddress.getByName("230.220.220.0");
        buf = "A small step for a java application, a huge step for the client".getBytes();

        DatagramPacket packet
                = new DatagramPacket(buf, buf.length, group, 8765);
        socket.send(packet);
        socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
