package com.dbs;

import com.dbs.filemanager.FileManager;
import com.dbs.messages.PutchunkMessage;
import com.dbs.messages.StoredMessage;

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
            FileManager.storeChunk(PeerController.getInstance().getBackupDir(), msg.getFileId(), msg.getChunkNo(), msg.getBody());
            sendResponse(msg);
        }
    }

    private void sendResponse(PutchunkMessage PCmessage) {

        sendSTORED(PCmessage);
    }

    private PutchunkMessage decodePUTCHUNK(byte[] encodedData){

        return PutchunkMessage.fromString(encodedData);

    }

    //THIS IS PROBABLY WRONG PLX FIX -- maybe fixed now?
    private void sendSTORED(PutchunkMessage receivedPUTCHUNK){
        StoredMessage msg = new StoredMessage(receivedPUTCHUNK.getVersion(), Peer.PEER_ID, receivedPUTCHUNK.getFileId(), receivedPUTCHUNK.getChunkNo());

        msg.send(PeerController.getInstance().getConnectionInfo().getControlChannelSocket(),
                PeerController.getInstance().getConnectionInfo().getControlChannelHostname(),
                PeerController.getInstance().getConnectionInfo().getControlPort());

    }

}
