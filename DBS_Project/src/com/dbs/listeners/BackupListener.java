package com.dbs.listeners;

import com.dbs.PeerConnectionInfo;
import com.dbs.PeerController;
import com.dbs.Storer;
import com.dbs.messages.PutchunkMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.concurrent.ThreadPoolExecutor;

public class BackupListener extends Listener {


    private final Storer storer;

    public BackupListener(MulticastSocket socket, ThreadPoolExecutor threadPool, String backup_dir) {
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
        //ONLY ALLOWING PUTCHUNKS PLX CHANGE
        PutchunkMessage msg = PutchunkMessage.fromString(Arrays.copyOf(packet.getData(), packet.getLength()));
        this.storer.store(msg);
    }
}
