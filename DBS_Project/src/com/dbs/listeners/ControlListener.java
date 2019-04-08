package com.dbs.listeners;

import com.dbs.*;
import com.dbs.messages.PeerMessage;
import com.dbs.messages.StoredMessage;

import java.net.DatagramPacket;

public class ControlListener extends Listener {


    public ControlListener() {
        super(PeerController.getInstance().getConnectionInfo().getControlChannelCommunicator(), PeerController.getInstance().getThreadPool());
    }

    @Override
    protected void processPacket(DatagramPacket packet) {
        String msgType = PeerMessage.getMessageType(new String(packet.getData(), 0, packet.getLength()));


        switch (msgType) {
            case "STORED":
                processStoredMsg(packet);
                break;
        }
    }

    private void processStoredMsg(DatagramPacket packet) {

        try {
            StoredMessage msg = StoredMessage.fromString(new String(packet.getData(), 0, packet.getLength()).getBytes());

            TaskLogKey key = new TaskLogKey(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.STORE);

            if(!msg.getSenderId().equals(Peer.PEER_ID)) {
                PeerController.getInstance().getTasks().get(key).addPeer(msg.getSenderId());
                TaskLogKey futureKey = new TaskLogKey(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.PUTCHUNK);
                if(PeerController.getInstance().replicationDegreeReached(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.STORE)) {

                    PeerController.getInstance().getTaskFutures().get(futureKey).cancel(true);
                    PeerController.getInstance().getTaskFutures().remove(futureKey);
                }
            }
        } catch(IllegalStateException e) {
            //Invalid msg format
        }







    }








}
