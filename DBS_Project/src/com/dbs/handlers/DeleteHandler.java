package com.dbs.handlers;

import com.dbs.Peer;
import com.dbs.filemanager.FileManager;
import com.dbs.messages.DeleteMessage;

import java.io.IOException;
import java.nio.file.Paths;

public class DeleteHandler {

    public DeleteHandler(String filePath) {
        try {
            this.fileId = FileManager.calcFileId(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String fileId;

    public void run() {
        new DeleteMessage(
                Peer.VERSION.getBytes(),
                Peer.PEER_ID,
                fileId
        ).send();
    }
}
