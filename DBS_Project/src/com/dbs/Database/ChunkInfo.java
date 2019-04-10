package com.dbs.Database;

import com.dbs.utils.Logger;

import java.util.HashSet;

public class ChunkInfo {

    private HashSet<Integer> peers = new HashSet<>();
    private int desiredReplication;

    /*public ChunkInfo(HashSet<Integer> peers, int desiredReplication) {
        this.peers = peers;
        this.desiredReplication = desiredReplication;
    }

    public ChunkInfo(HashSet<Integer> peers, byte[] data) {
        this.peers = peers;
        this.data = data;
    }*/

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




    /// SETTERS - Each should call saveChunksInformation

    public ChunkInfo addPeer(String senderId) {
        this.peers.add(Integer.parseInt(senderId));
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
}
