package com.dbs;

import com.dbs.filemanager.FileManager;
import com.dbs.messages.PutchunkMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Storer {

    private final String BACKUP_DIR;

    public Storer(String dir) {
        this.BACKUP_DIR = dir;
    }

    public void store(PutchunkMessage msg){
        if(Integer.parseInt(msg.getSenderId()) != PeerController.connectionInfo.getSenderId()) {
            FileManager.storeChunk(this.BACKUP_DIR, msg.getFileId(), msg.getChunkNo(), msg.getBody());
            sendResponse(msg);
        }
    }

    private void sendResponse(PutchunkMessage PCmessage) {
        int random = (int) Math.random()*400;

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        executor.schedule(()-> sendSTORED(PCmessage), random, TimeUnit.MILLISECONDS);

        executor.shutdown();
    }

    private PutchunkMessage decodePUTCHUNK(byte[] encodedData){

        return PutchunkMessage.fromString(encodedData);

    }

    private void sendSTORED(PutchunkMessage receivedPUTCHUNK){
        byte[] buf = "STORED <Version> <SenderId> <FileId> <ChunkNo> <CRLF><CRLF>".getBytes();

        try {
            DatagramPacket packet = new DatagramPacket(buf, buf.length,
                InetAddress.getByName(PeerController.connectionInfo.getControlChannelHostname()), PeerController.connectionInfo.getControlPort());
                PeerController.connectionInfo.getControlChannelSocket().send(packet);
        }catch(UnknownHostException e){
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }
    }

}
