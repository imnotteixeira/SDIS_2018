package com.dbs.Database;

import com.dbs.Peer;
import com.dbs.utils.Logger;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class ChunkInfo implements Serializable {

    private HashSet<Integer> peers = new HashSet<>();
    private int desiredReplication;

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


    /// SETTERS - Each should call saveChunksInformation

    public ChunkInfo addPeer(String senderId) {
        this.peers.add(Integer.parseInt(senderId));
        saveChunksInformation();
        return this;
    }

    public ChunkInfo removePeer(String senderId) {
        this.peers.remove(Integer.parseInt(senderId));
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

    public HashSet<Integer> getPeers() {
        return peers;
    }
}
