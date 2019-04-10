package com.dbs.handlers;

import com.dbs.*;
import com.dbs.filemanager.FileManager;
import com.dbs.messages.ChunkMessage;
import com.dbs.messages.GetchunkMessage;
import com.dbs.utils.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class GetchunkHandler {

    private final static int[] DELAY_PER_ATTEMPT = {0, 1, 2, 4, 8};
    private static final int MAX_RETRIES = 4;

    private Path outputFilePath;
    private String fileId;
    private int chunkNo = 0;
    private TaskLogKey key;
    private int nRetries = 0;
    private boolean completedTransfer = false;

    public GetchunkHandler(String filePath) {
        this.outputFilePath = Paths.get(filePath + ".recovered");
        FileManager.emptyFileIfExists(outputFilePath);
        try {
            fileId = FileManager.calcFileId(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void run() {

        if(nRetries > MAX_RETRIES) {
            Logger.log("Could not send getchunk. Exceeded number of retries");
            return;
        }

        ScheduledFuture future = PeerController.getInstance().getThreadPool().schedule((Runnable) this::sendGetchunk, DELAY_PER_ATTEMPT[nRetries], TimeUnit.SECONDS);

        Logger.log("Getchunk sent! Waiting " + DELAY_PER_ATTEMPT[nRetries] + "s to try again");

        key = new TaskLogKey(fileId, chunkNo, TaskType.GETCHUNK);

        PeerController.getInstance().getTaskFutures().put(key, future);
    }

    public void sendGetchunk() {

        GetchunkMessage msg = new GetchunkMessage(
                Peer.VERSION.getBytes(),
                Peer.PEER_ID,
                fileId,
                String.valueOf(chunkNo)
        );

        msg.send();

        nRetries++;

        if(!completedTransfer) {
            run();
        }
    }

    public void processReceivedChunk(ChunkMessage msg) {

        if(chunkNo == Integer.parseInt(msg.getChunkNo())){

            PeerController.getInstance().getTaskFutures().get(key).cancel(true);

            nRetries = 0;

            System.out.println("Received CHUNK number " + msg.getChunkNo());

            FileManager.appendChunkToFile(outputFilePath, msg.getBody());

            if(msg.getBody().length < PeerController.getInstance().CHUNK_SIZE) {
                PeerController.getInstance().removeGetchunkHandler(this.fileId);
                completedTransfer = true;
                Logger.log("File " + fileId +  " recovered successfully!");
            } else {
                chunkNo++;
                sendGetchunk();
            }

        }
        //was not waiting for this chunk
    }

    public String getFileId() {
        return fileId;
    }

}
