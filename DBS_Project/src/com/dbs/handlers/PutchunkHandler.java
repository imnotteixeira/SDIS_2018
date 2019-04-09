package com.dbs.handlers;

import com.dbs.PeerController;
import com.dbs.TaskLogKey;
import com.dbs.TaskType;
import com.dbs.messages.PutchunkMessage;
import com.dbs.utils.Logger;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PutchunkHandler {

    private final static int[] DELAY_PER_ATTEMPT = {0, 1, 2, 4, 8};
    private final String fileId;
    private final int chunkNo;
    private final int replicationDegree;
    private final byte[] chunk;
    private int nRetries;

    private static final int MAX_RETRIES = 4;

    public PutchunkHandler(String fileId, int chunkNo, int replicationDegree, byte[] chunk) {
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDegree = replicationDegree;
        this.chunk = chunk;


        this.nRetries = 0;
    }

    public void send() {
        if(nRetries > MAX_RETRIES) {
            if(!PeerController.getInstance().replicationDegreeReached(fileId, chunkNo, TaskType.STORE)) {
                Logger.log("Replication Degree was not reached. " +
                        "Please send information to remove backup from accepted Peers");
            }
            return;
        }

        TaskLogKey key = new TaskLogKey(fileId, chunkNo, TaskType.PUTCHUNK);



        ScheduledFuture future = PeerController.getInstance().getThreadPool().schedule((Runnable) this::sendPutchunk, DELAY_PER_ATTEMPT[nRetries], TimeUnit.SECONDS);
        Logger.log("Putchunk Sent! Waiting " + DELAY_PER_ATTEMPT[nRetries] + "s to try again");

        PeerController.getInstance().getTaskFutures().put(key, future);
    }



    private void sendPutchunk() {

        PutchunkMessage msg = new PutchunkMessage(PeerController.getInstance().getConnectionInfo().getVersion(),
                String.valueOf(PeerController.getInstance().getConnectionInfo().getSenderId()),
                fileId,
                String.valueOf(chunkNo),
                String.valueOf(replicationDegree),
                chunk
        );


        msg.send();



        this.nRetries++;
        this.send();
    }
}
