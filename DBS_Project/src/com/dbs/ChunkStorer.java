package com.dbs;

import com.dbs.Database.ChunkInfoStorer;
import com.dbs.filemanager.FileManager;
import com.dbs.messages.PutchunkMessage;
import com.dbs.messages.StoredMessage;
import com.dbs.utils.Logger;


public class ChunkStorer {

    public void store(PutchunkMessage msg){

        if(Integer.parseInt(msg.getSenderId()) != PeerController.connectionInfo.getSenderId()
                && ChunkInfoStorer.getInstance().getUsedBytes() + msg.getBody().length <= PeerController.getInstance().ALLOCATED_SPACE_KB * 1000) {

            FileManager.storeChunk(msg.getFileId(), msg.getChunkNo(), msg.getBody());
            sendStoredMsg(msg);

            //Storing the fact that this peer has the chunk, because it won't parse the incoming STORED message
            ChunkInfoStorer.getInstance().getChunkInfo(msg.getFileId(), msg.getChunkNo()).addPeer(Peer.PEER_ID).setBodySize(msg.getBody().length);

            Logger.log("Stored chunk! - from putchunk of peer " + msg.getSenderId());

        }
    }


    public void sendStoredMsg(PutchunkMessage receivedPUTCHUNK){
        StoredMessage msg = new StoredMessage(receivedPUTCHUNK.getVersion(), Peer.PEER_ID, receivedPUTCHUNK.getFileId(), receivedPUTCHUNK.getChunkNo());

        msg.send();
    }

}