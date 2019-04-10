package com.dbs.listeners;

import com.dbs.*;
import com.dbs.Database.ChunkInfo;
import com.dbs.Database.ChunkInfoStorer;
import com.dbs.messages.ChunkMessage;

import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.HashSet;

public class RecoveryListener extends Listener {

    public RecoveryListener() {
        super(PeerController.getInstance().getConnectionInfo().getRecoveryChannelCommunicator(), PeerController.getInstance().getThreadPool());
    }

    @Override
    protected void processPacket(DatagramPacket packet) {
        try {
            ChunkMessage msg = ChunkMessage.fromString(Arrays.copyOf(packet.getData(), packet.getLength()));

            TaskLogKey key = new TaskLogKey(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.CHUNK);

            //When receiving a CHUNK for a past GETCHUNK, cancel the CHUNK sending of this peer
            if(PeerController.getInstance().getTaskFutures().containsKey(key)) {
                PeerController.getInstance().getTaskFutures().get(key).cancel(true);
                PeerController.getInstance().getTaskFutures().remove(key);
            }

            try {
                PeerController.getInstance().getChunkHandlerForFile(msg.getFileId()).processReceivedChunk(msg);
            }catch (NullPointerException e) {
                //no handler in this peer waiting for chunks of this file
            }

        } catch(IllegalStateException e) {
            //No match found - invalid msg format
        }
    }
}
