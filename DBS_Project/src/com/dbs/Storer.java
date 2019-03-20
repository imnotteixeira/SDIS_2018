package com.dbs;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Storer {

    private PeerConnectionInfo connectionInfo;
    private static final int BUF_SIZE = 65622;


    public Storer(PeerConnectionInfo connectionInfo){
        this.connectionInfo = connectionInfo;
    }

    public void run(){
        while(true){
            PUTCHUNKListener();
        }
    }


    private DatagramPacket PUTCHUNKListener(){
        byte[] buf = new byte[Storer.BUF_SIZE];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        try {
            this.connectionInfo.getControlChanelSocket().receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }

        decodePUTCHUNK(new String(packet.getData(), 0, packet.getLength()));

        return packet;
    }

    private void decodePUTCHUNK(String encodedData){
        Pattern r = Pattern.compile("(PUTCHUNK)\\s(\\d.\\d)\\s(\\d+)\\s(\\w+)\\s(\\d{1,6})\\s(\\d)");

        Matcher m = r.matcher(encodedData);


    }

}
