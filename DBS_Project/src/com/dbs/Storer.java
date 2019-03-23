package com.dbs;

import com.dbs.messages.PutchunkMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Storer {

    private PeerConnectionInfo connectionInfo;
    private static final int BUF_SIZE = 65622;


    public Storer(PeerConnectionInfo connectionInfo){
        this.connectionInfo = connectionInfo;
    }

    public void run(){
        while(true){
            PutchunkMessage PCmessage = PUTCHUNKListener();
            sendResponse(PCmessage);
        }
    }

    private PutchunkMessage PUTCHUNKListener(){
        byte[] buf = new byte[Storer.BUF_SIZE];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        try {
            this.connectionInfo.getControlChannelSocket().receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Received Control Message: " + new String(packet.getData(), 0, packet.getLength()));

        return decodePUTCHUNK(new String(packet.getData(), 0, packet.getLength()));
    }

    private void sendResponse(PutchunkMessage PCmessage) {
        int random = (int) Math.random()*400;

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        executor.schedule(()-> sendSTORED(PCmessage), random, TimeUnit.MILLISECONDS);

        executor.shutdown();
    }

    private PutchunkMessage decodePUTCHUNK(String encodedData){

        return PutchunkMessage.fromString(encodedData);

    }

    private void sendSTORED(PutchunkMessage receivedPUTCHUNK){
        byte[] buf = "STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>".getBytes();

        try {
            DatagramPacket packet = new DatagramPacket(buf, buf.length,
                InetAddress.getByName(connectionInfo.getControlChannelHostname()), connectionInfo.getControlPort());
                connectionInfo.getControlChannelSocket().send(packet);
        }catch(UnknownHostException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

}
