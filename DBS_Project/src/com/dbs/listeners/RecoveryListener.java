package com.dbs.listeners;

import com.dbs.*;
import com.dbs.Database.ChunkInfo;
import com.dbs.Database.ChunkInfoStorer;
import com.dbs.messages.ChunkMessage;
import com.dbs.messages.PeerMessage;
import com.dbs.messages.TCPSocketChunkMessage;
import com.dbs.utils.Logger;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;

public class RecoveryListener extends Listener {

    public RecoveryListener() {
        super(PeerController.connectionInfo.getRecoveryChannelCommunicator(), PeerController.getInstance().getThreadPool());
    }

    @Override
    protected void processPacket(DatagramPacket packet) {

        //check msg type
        String version = PeerMessage.getMsgProtocolVersion(Arrays.copyOf(packet.getData(), packet.getLength()));

        if(version.equals("1.0")) {
            processPacket_Base(packet);
        } else if (version.equals("1.1")) {
            processPacket_Enhancement(packet);
        }

    }

    private void processPacket_Base(DatagramPacket packet) {
        try {
            ChunkMessage msg = ChunkMessage.fromString(Arrays.copyOf(packet.getData(), packet.getLength()));

            TaskLogKey key = new TaskLogKey(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.CHUNK);

            //When receiving a CHUNK for a past GETCHUNK, cancel the CHUNK sending of this peer
            if(PeerController.getInstance().getTaskFutures().containsKey(key)) {
                PeerController.getInstance().getTaskFutures().get(key).cancel(true);
                PeerController.getInstance().getTaskFutures().remove(key);
            }

            try {
                PeerController.getInstance().getChunkHandlerForFile(msg.getFileId()).processReceivedChunk(msg);
            }catch (NullPointerException e) {
                //no handler in this peer waiting for chunks of this file
            }

        } catch(IllegalStateException e) {
            //No match found - invalid msg format
        }
    }

    private void processPacket_Enhancement(DatagramPacket packet) {

        TCPSocketChunkMessage msg = TCPSocketChunkMessage.fromString(Arrays.copyOf(packet.getData(), packet.getLength()));

        String hostname = msg.getHostname();
        int port = msg.getPort();

        Socket clientSocket = null;
        try {
            clientSocket = new Socket(InetAddress.getByName(hostname), port);


            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            byte[] chunkBody = (byte[]) in.readObject();


            in.close();
            clientSocket.close();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }



    }


}
