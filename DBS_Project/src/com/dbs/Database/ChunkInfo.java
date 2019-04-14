package com.dbs.Database;

import com.dbs.Peer;

import java.io.Serializable;
import java.util.HashSet;

public class ChunkInfo implements Serializable {

    private HashSet<Integer> peers = new HashSet<>();
    private int desiredReplication;
    private int bodySize = 0; //value is only set when chunk is stored in this peer

    private static void saveChunksInformation(){
        ChunkInfoStorer.getInstance().save();
    }


    //// GETTERS


    public int getPerceivedReplication(){
        return this.peers.size();
    }

    public boolean isReplicationReached(){

        return this.getPerceivedReplication() >= this.getDesiredReplication();
    }

    public int getDesiredReplication() {
        return desiredReplication;
    }

    public boolean isStored(){
        return peers.contains(Integer.parseInt(Peer.PEER_ID));
    }

    public int getBodySize(){
        return this.bodySize;
    }


    /// SETTERS - Each should call saveChunksInformation

    public ChunkInfo addPeer(String senderId) {
        this.peers.add(Integer.parseInt(senderId));
        if(senderId == Peer.PEER_ID) {
            ChunkInfoStorer.getInstance().updateStorageSize(getBodySize());
        }
        saveChunksInformation();
        return this;
    }

    public ChunkInfo removePeer(String senderId) {
        this.peers.remove(Integer.parseInt(senderId));
        if(senderId == Peer.PEER_ID) {
            ChunkInfoStorer.getInstance().updateStorageSize(-getBodySize());
        }
        saveChunksInformation();
        return this;
    }

    public ChunkInfo setDesiredReplicationDegree(String replicationDegree) {
        return setDesiredReplicationDegree(Integer.parseInt(replicationDegree));
    }

    public ChunkInfo setDesiredReplicationDegree(int replicationDegree) {
        this.desiredReplication = replicationDegree;
        saveChunksInformation();
        return this;
    }

    public ChunkInfo setBodySize(int size) {
        this.bodySize = size;
        return this;
    }

    @Override
    public String toString() {
        return "Perceived Replication Factor: " + this.getPerceivedReplication()
                + ", Body Size: " + this.bodySize + " bytes";
    }
}
