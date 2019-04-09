package com.dbs.listeners;

import com.dbs.*;
import com.dbs.filemanager.FileManager;
import com.dbs.messages.ChunkMessage;
import com.dbs.messages.GetchunkMessage;
import com.dbs.messages.PeerMessage;
import com.dbs.messages.StoredMessage;
import com.dbs.utils.Logger;

import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ControlListener extends Listener {


    public ControlListener() {
        super(PeerController.getInstance().getConnectionInfo().getControlChannelCommunicator(), PeerController.getInstance().getThreadPool());
    }

    @Override
    protected void processPacket(DatagramPacket packet) {
        String msgType = PeerMessage.getMessageType(new String(packet.getData(), 0, packet.getLength()));


        switch (msgType) {
            case "STORED":
                processStoredMsg(packet);
                break;
            case "GETCHUNK":
                processGetchunkMsg(packet);
                break;
        }
    }

    private void processGetchunkMsg(DatagramPacket packet) {
        try {
            GetchunkMessage msg = GetchunkMessage.fromString(new String(packet.getData(), 0, packet.getLength()).getBytes());

            TaskLogKey key = new TaskLogKey(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.CHUNK);


            int randomWaitTime = (int) (Math.random() * 400);

            Logger.log("Waiting " + randomWaitTime + " ms before trying to send CHUNK!");

            TaskLogKey futureKey = new TaskLogKey(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.CHUNK);

            // Send CHUNK in <randomTime> ms, if not canceled before
            ScheduledFuture future = threadPool.schedule(()->this.processRecovery(key), randomWaitTime, TimeUnit.MILLISECONDS);

            PeerController.getInstance().getTaskFutures().put(futureKey, future);




//            TaskLogKey futureKey = new TaskLogKey(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.PUTCHUNK);
//            if(PeerController.getInstance().replicationDegreeReached(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.CHUNK)) {
//
//                PeerController.getInstance().getTaskFutures().get(futureKey).cancel(true);
//                PeerController.getInstance().getTaskFutures().remove(futureKey);
//            }

        } catch(IllegalStateException e) {
            //Invalid msg format
        }
    }



    private void processStoredMsg(DatagramPacket packet) {

        try {
            StoredMessage msg = StoredMessage.fromString(new String(packet.getData(), 0, packet.getLength()).getBytes());

            TaskLogKey key = new TaskLogKey(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.STORE);


            PeerController.getInstance().getTasks().get(key).addPeer(msg.getSenderId());
            TaskLogKey futureKey = new TaskLogKey(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.PUTCHUNK);
            if(PeerController.getInstance().replicationDegreeReached(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.STORE)) {

                PeerController.getInstance().getTaskFutures().get(futureKey).cancel(true);
                PeerController.getInstance().getTaskFutures().remove(futureKey);
            }

        } catch(IllegalStateException e) {
            //Invalid msg format
        }

    }

    private void processRecovery(TaskLogKey key) {

        PeerController.getInstance().getTaskFutures().remove(key);


        byte[] chunkData;
        try {
            chunkData = FileManager.getChunk(PeerController.getInstance().getBackupDir(), key.fileId, key.chunkNo);
        } catch (FileNotFoundException e) {
            //This Peer does not have the requested File Chunk
            return;
        }

        ChunkMessage msg = new ChunkMessage(
                Peer.VERSION.getBytes(),
                Peer.PEER_ID,
                key.fileId,
                String.valueOf(key.chunkNo),
                chunkData
        );

        msg.send();

        Logger.log("Sent CHUNK message for file: " + key.fileId);

    }







}
