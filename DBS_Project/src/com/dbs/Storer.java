package com.dbs;

import com.dbs.filemanager.FileManager;
import com.dbs.messages.PutchunkMessage;
import com.dbs.messages.StoredMessage;


public class Storer {

    public void store(PutchunkMessage msg){
        if(Integer.parseInt(msg.getSenderId()) != PeerController.connectionInfo.getSenderId()) {

            FileManager.storeChunk(PeerController.getInstance().getBackupDir(), msg.getFileId(), msg.getChunkNo(), msg.getBody());
            sendStoredMsg(msg);
        }
    }

    //THIS IS PROBABLY WRONG PLX FIX -- maybe fixed now?
    private void sendStoredMsg(PutchunkMessage receivedPUTCHUNK){
        StoredMessage msg = new StoredMessage(receivedPUTCHUNK.getVersion(), Peer.PEER_ID, receivedPUTCHUNK.getFileId(), receivedPUTCHUNK.getChunkNo());

        msg.send(PeerController.getInstance().getConnectionInfo().getControlChannelHostname(),
                PeerController.getInstance().getConnectionInfo().getControlPort());

    }

}
