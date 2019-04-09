package com.dbs;

import com.dbs.filemanager.FileManager;
import com.dbs.messages.PutchunkMessage;
import com.dbs.messages.StoredMessage;


public class Storer {

    public void store(PutchunkMessage msg){
        if(Integer.parseInt(msg.getSenderId()) != PeerController.connectionInfo.getSenderId()) {

            FileManager.storeChunk(PeerController.getInstance().getBackupDir(), msg.getFileId(), msg.getChunkNo(), msg.getBody());
            sendStoredMsg(msg);

            //Storing the fact that this peer has the chunk, because it won't parse the incoming STORED message
            TaskLogKey key = new TaskLogKey(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.STORE);
            PeerController.getInstance().getTasks().get(key).addPeer(msg.getSenderId());
        }
    }


    private void sendStoredMsg(PutchunkMessage receivedPUTCHUNK){
        StoredMessage msg = new StoredMessage(receivedPUTCHUNK.getVersion(), Peer.PEER_ID, receivedPUTCHUNK.getFileId(), receivedPUTCHUNK.getChunkNo());

        msg.send();

    }

}
