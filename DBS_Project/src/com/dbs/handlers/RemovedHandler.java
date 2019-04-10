package com.dbs.handlers;

import com.dbs.*;
import com.dbs.filemanager.FileManager;
import com.dbs.messages.RemovedMessage;

import java.io.IOException;
import java.nio.file.Paths;

public class RemovedHandler {

    private String fileId;

    public RemovedHandler(String filePath) {
        try {
            this.fileId = FileManager.calcFileId(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
