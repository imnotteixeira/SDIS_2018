package com.dbs;

import java.util.HashSet;

public class ChunkInfo {

    public HashSet<Integer> peers;
    public int desiredReplication;
    public byte[] data;

    public ChunkInfo(HashSet<Integer> peers, int desiredReplication) {
        this.peers = peers;
        this.desiredReplication = desiredReplication;
    }

    public ChunkInfo(HashSet<Integer> peers, byte[] data) {
        this.peers = peers;
        this.data = data;
    }

    public HashSet<Integer> getPeers() {
        return peers;
    }

    public int getDesiredReplication() {
        return desiredReplication;
    }

    public byte[] getData() {
        return data;
    }

    public void addPeer(String senderId) {
        this.peers.add(Integer.parseInt(senderId));
    }

}
