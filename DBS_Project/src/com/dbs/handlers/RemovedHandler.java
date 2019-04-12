package com.dbs.handlers;

import com.dbs.*;
import com.dbs.messages.RemovedMessage;

public class RemovedHandler {

    private String fileId;

    public RemovedHandler(String fileId) {
        this.fileId = fileId;
    }

    public void run(int chunkNo) {
        new RemovedMessage(
                Peer.VERSION.getBytes(),
                Peer.PEER_ID,
                fileId,
                Integer.toString(chunkNo)
        ).send();
    }
}
