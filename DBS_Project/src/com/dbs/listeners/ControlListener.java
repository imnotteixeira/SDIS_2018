package com.dbs.listeners;

import com.dbs.*;
import com.dbs.Database.ChunkInfo;
import com.dbs.Database.ChunkInfoStorer;
import com.dbs.filemanager.FileManager;
import com.dbs.handlers.PutchunkHandler;
import com.dbs.messages.*;
import com.dbs.utils.Logger;
import javafx.concurrent.Task;

import javax.xml.crypto.Data;
import java.io.File;
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
            case "DELETE":
                processDeleteMsg(packet);
                break;
            case "REMOVED":
                processRemovedMsg(packet);
                break;
        }
    }

    private void processGetchunkMsg(DatagramPacket packet) {
        try {
            GetchunkMessage msg = GetchunkMessage.fromString(new String(packet.getData(), 0, packet.getLength()).getBytes());

            int randomWaitTime = (int) (Math.random() * 400);

            Logger.log("Waiting " + randomWaitTime + " ms before trying to send CHUNK!");

            TaskLogKey futureKey = new TaskLogKey(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.CHUNK);

            // Send CHUNK in <randomTime> ms, if not canceled before
            ScheduledFuture future = threadPool.schedule(()->this.processRecovery(futureKey), randomWaitTime, TimeUnit.MILLISECONDS);

            PeerController.getInstance().getTaskFutures().put(futureKey, future);

        } catch(IllegalStateException e) {
            //Invalid msg format
        }
    }



    private void processStoredMsg(DatagramPacket packet) {

        try {
            StoredMessage msg = StoredMessage.fromString(new String(packet.getData(), 0, packet.getLength()).getBytes());

            ChunkInfo chunkStatus = ChunkInfoStorer.getInstance().getChunkInfo(msg.getFileId(), msg.getChunkNo()).addPeer(msg.getSenderId());

            TaskLogKey futureKey = new TaskLogKey(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.PUTCHUNK);

            TaskLogKey futureKeyRemoved = new TaskLogKey(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.REMOVED);

            if(PeerController.getInstance().getTaskFutures().containsKey(futureKeyRemoved)){
                PeerController.getInstance().getTaskFutures().get(futureKey).cancel(true);
                PeerController.getInstance().getTaskFutures().remove(futureKey);
            }

            if(chunkStatus.isReplicationReached()) {
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
            chunkData = FileManager.getChunk(key.fileId, key.chunkNo);
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


    private void processDeleteMsg(DatagramPacket packet) {

        DeleteMessage msg = DeleteMessage.fromString(packet.getData());

        if(FileManager.deleteBackupFolder(msg.getFileId())){
            Logger.log("Successfully deleted file");
        }else{
            Logger.log("No chunks to delete");
        }
    }

    private void processRemovedMsg(DatagramPacket packet){

        RemovedMessage msg = RemovedMessage.fromString(packet.getData());

        ChunkInfoStorer.getInstance().getChunkInfo(msg.getFileId(), msg.getChunkNo())
            .removePeer(msg.getSenderId());

        try {
            int randomWaitTime = (int) (Math.random() * 400);

            Logger.log("Peer " + msg.getSenderId() + " has deleted a chunk. Going to wait " + randomWaitTime + "s and send a putchunk");

            // Send CHUNK in <randomTime> ms, if not canceled beforef not canceled before

            TaskLogKey futureKey = new TaskLogKey(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.REMOVED);

            ScheduledFuture future = threadPool.schedule(()->this.processRemovedChunk(futureKey), randomWaitTime, TimeUnit.MILLISECONDS);

            PeerController.getInstance().getTaskFutures().put(futureKey, future);

        } catch(IllegalStateException e) {
            //Invalid msg format
        }

    }

    private void processRemovedChunk(TaskLogKey key) {
        try {
            PutchunkHandler handler = new PutchunkHandler(key.fileId, key.chunkNo, 1, FileManager.getChunk(key.fileId, key.chunkNo));
            handler.send();
        } catch (FileNotFoundException e) {
            //this peer does not have this chunk and so cannot send it
        }
    }

}
