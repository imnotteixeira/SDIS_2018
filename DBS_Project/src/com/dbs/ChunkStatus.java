package com.dbs;

import java.util.HashSet;

public class ChunkStatus {

    public HashSet<Integer> peers;
    public int desiredReplication;

    public ChunkStatus(HashSet<Integer> peers, int desiredReplication) {
        this.peers = peers;
        this.desiredReplication = desiredReplication;
    }

    public HashSet<Integer> getPeers() {
        return peers;
    }

    public int getDesiredReplication() {
        return desiredReplication;
    }

    public void addPeer(String senderId) {
        this.peers.add(Integer.parseInt(senderId));
    }

    @Override
    public String toString() {
        return "ChunkStatus{" +
                "n_peers=" + peers.size() +
                ", desiredReplication=" + desiredReplication +
                '}';
    }
}
