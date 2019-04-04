package com.dbs.listeners;

import com.dbs.ChunkStatus;
import com.dbs.PeerController;
import com.dbs.TaskLogKey;
import com.dbs.TaskType;
import com.dbs.messages.PeerMessage;
import com.dbs.messages.StoredMessage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

public class ControlListener extends Listener {


    public ControlListener(MulticastSocket socket, ScheduledExecutorService threadPool) {
        super(socket, threadPool);
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
        String msgType = PeerMessage.getMessageType(new String(packet.getData(), 0, packet.getLength()));


        switch (msgType) {
            case "STORED":
                processStoredMsg(packet);
                break;
        }
    }

    private void processStoredMsg(DatagramPacket packet) {

        StoredMessage msg = StoredMessage.fromString(new String(packet.getData(), 0, packet.getLength()).getBytes());

        TaskLogKey key = new TaskLogKey(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.STORE);

        PeerController.getInstance().getTasks().get(key).addPeer(msg.getSenderId());




    }








}
