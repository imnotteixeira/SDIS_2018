package com.dbs.listeners;

import com.dbs.*;
import com.dbs.messages.PutchunkMessage;
import com.dbs.utils.Logger;

import java.net.DatagramPacket;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BackupListener extends Listener {


    private final Storer storer;

    public BackupListener() {
        super(
                PeerController.getInstance().getConnectionInfo().getBackupChannelCommunicator(),
                PeerController.getInstance().getThreadPool()
        );
        this.storer = new Storer();
    }

    @Override
    protected void processPacket(DatagramPacket packet) {
        try {
            PutchunkMessage msg = PutchunkMessage.fromString(Arrays.copyOf(packet.getData(), packet.getLength()));

            TaskLogKey key = new TaskLogKey(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.STORE);
            ChunkInfo value = new ChunkInfo(new HashSet<>(), Integer.parseInt(msg.getReplicationDegree()));
            PeerController.getInstance().getTasks().put(key, value);

            int randomWaitTime = (int) (Math.random() * 400);

            Logger.log("Waiting " + randomWaitTime + " ms before trying to store chunk!");

            threadPool.schedule(()->this.processStorage(key, msg), randomWaitTime, TimeUnit.MILLISECONDS);

        } catch(IllegalStateException e) {
            //No match found - invalid msg format
        }


    }

    private void processStorage(TaskLogKey key, PutchunkMessage msg) {


        ChunkInfo status = PeerController.getInstance().getTasks().get(key);


//        System.out.println("The current replication degree is " + status.getPeers().size() + ". The desired one is " + status.desiredReplication);
        if(status.getPeers().size() < status.desiredReplication) {
//            System.out.println("Processing PUTCHUNK Sender ID: "+ msg.getSenderId() + "| peer_id: " + Peer.PEER_ID);
//            System.out.println("Sending STORED!");

            this.storer.store(msg);

        }
    }
}
