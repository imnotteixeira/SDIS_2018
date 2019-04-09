package com.dbs.listeners;

import com.dbs.*;
import com.dbs.messages.ChunkMessage;
import com.dbs.utils.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.Arrays;
import java.util.HashSet;

public class RecoveryListener extends Listener {

    public RecoveryListener() {
        super(PeerController.getInstance().getConnectionInfo().getControlChannelCommunicator(), PeerController.getInstance().getThreadPool());
    }

    @Override
    protected void processPacket(DatagramPacket packet) {
        try {
            ChunkMessage msg = ChunkMessage.fromString(Arrays.copyOf(packet.getData(), packet.getLength()));

            TaskLogKey key = new TaskLogKey(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.CHUNK);
            ChunkInfo value = new ChunkInfo(new HashSet<>(), msg.getBody());
            PeerController.getInstance().getTasks().put(key, value);

            Logger.log("Received a recovered CHUNK!");

            //When receiving a CHUNK for a past GETCHUNK, cancel the CHUNK sending of this peer
            if(PeerController.getInstance().getTaskFutures().containsKey(key)) {
                PeerController.getInstance().getTaskFutures().get(key).cancel(true);
                PeerController.getInstance().getTaskFutures().remove(key);
            }

            PeerController.getInstance().processReceivedChunk(msg);

        } catch(IllegalStateException e) {
            //No match found - invalid msg format
        }
    }
}
