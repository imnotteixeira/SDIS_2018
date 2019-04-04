package com.dbs.listeners;

import com.dbs.*;
import com.dbs.messages.PutchunkMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BackupListener extends Listener {


    private final Storer storer;

    public BackupListener(MulticastSocket socket, ScheduledExecutorService threadPool, String backup_dir) {
        super(socket, threadPool);
        this.storer = new Storer(backup_dir);
    }

    @Override
    public void listen() {
        while(true) {
            byte[] buf = new byte[Listener.BUF_SIZE];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            try {
                socket.receive(packet);
                threadPool.submit(() -> processPacket(packet));
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    private void processPacket(DatagramPacket packet) {
        PutchunkMessage msg = PutchunkMessage.fromString(Arrays.copyOf(packet.getData(), packet.getLength()));


        TaskLogKey key = new TaskLogKey(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.STORE);
        ChunkStatus value = new ChunkStatus(new HashSet<>(), Integer.parseInt(msg.getReplicationDegree()));
        PeerController.getInstance().getTasks().put(key, value);

        int randomWaitTime = (int) (Math.random() * 400);

        System.out.println("Waiting " + randomWaitTime + " ms before trying to store chunk!");

        threadPool.schedule(()->this.processStorage(key, msg), randomWaitTime, TimeUnit.MILLISECONDS);
    }

    private void processStorage(TaskLogKey key, PutchunkMessage msg) {

        ChunkStatus status = PeerController.getInstance().getTasks().get(key);


        System.out.println("The current replication degree is " + status.getPeers().size() + ". The desired one is " + status.desiredReplication);
        if(status.getPeers().size() < status.desiredReplication) {
            System.out.println("Sending STORED!");
            this.storer.store(msg);

        }
    }
}
