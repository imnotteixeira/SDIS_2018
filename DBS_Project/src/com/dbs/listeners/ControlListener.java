package com.dbs.listeners;

import com.dbs.*;
import com.dbs.Database.ChunkInfo;
import com.dbs.Database.ChunkInfoStorer;
import com.dbs.filemanager.FileManager;
import com.dbs.handlers.PutchunkHandler;
import com.dbs.messages.*;
import com.dbs.utils.Logger;
import com.dbs.utils.NetworkAddress;

import java.io.*;
import java.net.*;
import java.io.FileNotFoundException;
import java.net.DatagramPacket;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ControlListener extends Listener {

    private static final int TCP_TIMEOUT_MS = 15000;

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
            case "GETCHUNK":
                processGetchunkMsg(packet);
                break;
            case "DELETE":
                processDeleteMsg(packet);
                break;
            case "REMOVED":
                processRemovedMsg(packet);
                break;
        }
    }

    private void processGetchunkMsg(DatagramPacket packet) {
        try {
            GetchunkMessage msg = GetchunkMessage.fromString(new String(packet.getData(), 0, packet.getLength()).getBytes());

            int randomWaitTime = (int) (Math.random() * 400);

            Logger.log("Waiting " + randomWaitTime + " ms before trying to send CHUNK!");

            TaskLogKey futureKey = new TaskLogKey(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.CHUNK);


            // Send CHUNK in <randomTime> ms, if not canceled before
            ScheduledFuture future = threadPool.schedule(()->this.processRecovery(futureKey, new String(msg.getVersion(), 0, msg.getVersion().length)), randomWaitTime, TimeUnit.MILLISECONDS);

            PeerController.getInstance().getTaskFutures().put(futureKey, future);

        } catch(IllegalStateException e) {
            //Invalid msg format
        }
    }



    private void processStoredMsg(DatagramPacket packet) {

        try {
            StoredMessage msg = StoredMessage.fromString(new String(packet.getData(), 0, packet.getLength()).getBytes());

            ChunkInfo chunkStatus = ChunkInfoStorer.getInstance().getChunkInfo(msg.getFileId(), msg.getChunkNo()).addPeer(msg.getSenderId());

            TaskLogKey sentPutchunkFutureKey = new TaskLogKey(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.PUTCHUNK);

            TaskLogKey futureKeyRemoved = new TaskLogKey(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.REMOVED);

            if(chunkStatus.isReplicationReached()) {
                PeerController.getInstance().getTaskFutures().get(sentPutchunkFutureKey).cancel(true);
                PeerController.getInstance().getTaskFutures().remove(sentPutchunkFutureKey);
                PeerController.getInstance().getTaskFutures().get(futureKeyRemoved).cancel(true);
                PeerController.getInstance().getTaskFutures().remove(futureKeyRemoved);
            }

        } catch(IllegalStateException e) {
            //Invalid msg format
        }

    }

    private void processRecovery(TaskLogKey key, String version) {

        PeerController.getInstance().getTaskFutures().remove(key);


        byte[] chunkData;
        try {
            chunkData = FileManager.getChunk(key.fileId, key.chunkNo);
        } catch (FileNotFoundException e) {
            //This Peer does not have the requested File Chunk
            return;
        }

        if(version.equals("1.0")) {
            processBaseVersionRecovery(key, chunkData);
        } else if(version.equals("1.1")) {
            processImprovedVersionRecovery(key, chunkData);
        }

    }

    private void processBaseVersionRecovery(TaskLogKey key, byte[] chunkData) {
        ChunkMessage msg = new ChunkMessage(
                "1.0".getBytes(),
                Peer.PEER_ID,
                key.fileId,
                String.valueOf(key.chunkNo),
                chunkData
        );

        msg.send();

        Logger.log("Sent CHUNK message for file: " + key.fileId);
    }

    private void processImprovedVersionRecovery(TaskLogKey key, byte[] chunkData) {
        //open tcp socket
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(0);
            serverSocket.setReuseAddress(true);
            serverSocket.setSoTimeout(TCP_TIMEOUT_MS);

        } catch (IOException e) {
            Logger.log("Could not create TCP socket to send CHUNK");
            return;
        }

        String hostname = NetworkAddress.getHostIp();

        //send msg to share tcp socket
        TCPSocketChunkMessage msg = new TCPSocketChunkMessage(
                Peer.PEER_ID,
                key.fileId,
                String.valueOf(key.chunkNo),
                hostname,
                serverSocket.getLocalPort()
        );

        msg.send();

        ObjectOutputStream out = null;
        Socket clientSocket = null;

        //wait for response
        try {
            clientSocket = serverSocket.accept();

            //when connection, send chunk data
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            out.writeObject(chunkData);




        } catch (SocketTimeoutException e) {
            Logger.log("Did not make connection to send chunk, releasing socket...");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(clientSocket != null) {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }


    }

    private void processDeleteMsg(DatagramPacket packet) {

        DeleteMessage msg = DeleteMessage.fromString(packet.getData());

        if(FileManager.deleteBackupFolder(msg.getFileId())){
            Logger.log("Successfully deleted file");
        }else{
            Logger.log("No chunks to delete");
        }
    }

    private void processRemovedMsg(DatagramPacket packet){

        RemovedMessage msg = RemovedMessage.fromString(packet.getData());

        ChunkInfoStorer.getInstance().getChunkInfo(msg.getFileId(), msg.getChunkNo())
            .removePeer(msg.getSenderId());

        Logger.log("New perceived: " + ChunkInfoStorer.getInstance().getChunkInfo(msg.getFileId(), msg.getChunkNo()).getPerceivedReplication());

        try {
            int randomWaitTime = (int) (Math.random() * 400);

            Logger.log("Peer " + msg.getSenderId() + " has deleted a chunk. Going to wait " + randomWaitTime + "s and send a putchunk");

            // Send CHUNK in <randomTime> ms, if not canceled beforef not canceled before

            TaskLogKey futureKey = new TaskLogKey(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.REMOVED);

            ScheduledFuture future = threadPool.schedule(()->this.processRemovedChunk(futureKey), randomWaitTime, TimeUnit.MILLISECONDS);

            PeerController.getInstance().getTaskFutures().put(futureKey, future);

        } catch(IllegalStateException e) {
            //Invalid msg format
        }

    }

    private void processRemovedChunk(TaskLogKey key) {
        try {
            PutchunkHandler handler = new PutchunkHandler(key.fileId, key.chunkNo, ChunkInfoStorer.getInstance().getChunkInfo(key.fileId, key.chunkNo).getDesiredReplication(), FileManager.getChunk(key.fileId, key.chunkNo));
            handler.send();
        } catch (FileNotFoundException e) {
            //this peer does not have this chunk and so cannot send it
        }
    }

}
