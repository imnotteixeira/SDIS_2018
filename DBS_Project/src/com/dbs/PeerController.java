package com.dbs;

import com.dbs.Database.ChunkInfo;
import com.dbs.Database.ChunkInfoStorer;
import com.dbs.Database.ChunkKey;
import com.dbs.filemanager.FileManager;
import com.dbs.handlers.DeleteHandler;
import com.dbs.handlers.GetchunkHandler;
import com.dbs.handlers.PutchunkHandler;
import com.dbs.handlers.RemovedHandler;
import com.dbs.listeners.BackupListener;
import com.dbs.listeners.ControlListener;
import com.dbs.listeners.Listener;
import com.dbs.listeners.RecoveryListener;
import com.dbs.utils.Logger;
import com.dbs.utils.NetworkAddress;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.*;

public class PeerController {

    public HashSet<String> compatibleProtocolVersions = new HashSet<>();
    public int ALLOCATED_SPACE_KB = 10000;
    public final int CHUNK_SIZE = (int) 64e3;

    private String rmi_name;
    public static PeerConnectionInfo connectionInfo;

    private Registry reg = null;
    private PeerRemoteObject peer_remote_object = null;

    private ConcurrentHashMap<TaskLogKey, ChunkInfo> tasks = new ConcurrentHashMap<>();
    private ConcurrentHashMap<TaskLogKey, ScheduledFuture> taskFutures = new ConcurrentHashMap<>();

    private HashMap<String, GetchunkHandler> getchunkHandlers = new HashMap<>();

    private ScheduledExecutorService threadPool;


    private static PeerController instance = null;

    public static PeerController getInstance() {

        if (instance == null) {
            instance = new PeerController(
                    Peer.VERSION,
                    Peer.PEER_ID,
                    Peer.RMI_NAME,
                    Peer.MC_ADDRESS,
                    Peer.MDB_ADDRESS,
                    Peer.MDR_ADDRESS
            );
        }

        return instance;
    }


    private PeerController(String version, String peer_id, String rmi_name, String mc_address, String mdb_address, String mdr_address) {

        this.rmi_name = rmi_name;

        this.compatibleProtocolVersions.add("1.0");
        if(version.equals("1.1")) {
            this.compatibleProtocolVersions.add("1.1");
        }

        NetworkAddress mc_address_addr = new NetworkAddress(mc_address);
        NetworkAddress mdb_address_addr = new NetworkAddress(mdb_address);
        NetworkAddress mdr_address_addr = new NetworkAddress(mdr_address);

        connectionInfo = new PeerConnectionInfo(
                Integer.valueOf(peer_id),
                version.getBytes(),
                mc_address_addr.hostname,
                mc_address_addr.port,
                mdb_address_addr.hostname,
                mdb_address_addr.port,
                mdr_address_addr.hostname,
                mdr_address_addr.port
        );

        initializePeerNetworkConnection();
    }

    public void start() {

        Logger.log("Starting Peer...");

        Logger.log("Version: " + Peer.VERSION);

        initializeRemoteObject();
        Logger.log("Initialized Remote Object.");


        Logger.log("Peer Ready.");

    }

    public void initializeRemoteObject() {
        this.peer_remote_object = new PeerRemoteObject(this);
        IPeerInterface stub = null;

        try {
            stub = (IPeerInterface) UnicastRemoteObject.exportObject(this.peer_remote_object, 0);

            try {
                this.reg = LocateRegistry.getRegistry(1099);

                this.reg.rebind(this.rmi_name, stub);

            } catch (RemoteException e) {

                this.reg = LocateRegistry.createRegistry(1099);

                this.reg.rebind(this.rmi_name, stub);
            }

        } catch (RemoteException e) {
            e.printStackTrace();

        }

    }

    public void initializePeerNetworkConnection() {

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        this.threadPool = Executors.newScheduledThreadPool(10);

        Future<String> controlChannelListener = executorService.submit(() -> startControlChannelListener());
        Future<String> backupChannelListener = executorService.submit(() -> startBackupChannelListener());
        Future<String> recoveryChannelListener = executorService.submit(() -> startRestoreChannelListener());

    }

    private String startControlChannelListener() {
        try {
            //Connect to Control Channel
            MulticastSocket socket = new MulticastSocket(connectionInfo.getControlPort());

            InetAddress mc_group = InetAddress.getByName(connectionInfo.getControlChannelHostname());
            socket.joinGroup(mc_group);

            connectionInfo.setControlChannelCommunicator(new Communicator(socket));
            Logger.log("Joined Control Channel at " + connectionInfo.getControlChannelHostname() + ":" + connectionInfo.getControlPort());
        } catch (IOException e) {
            return "IO ERROR";
        }

        //START LISTENING HERE
        Listener controlChannelListener = new ControlListener();
        Logger.log("Listening for control messages...");
        controlChannelListener.listen();


        return "";
    }

    private String startBackupChannelListener() {
        try {

            //Connect to Backup Channel
            MulticastSocket socket = new MulticastSocket(connectionInfo.getBackupPort());

            connectionInfo.setBackupChannelCommunicator(new Communicator(socket));
            InetAddress mdb_group = InetAddress.getByName(connectionInfo.getBackupChannelHostname());
            socket.joinGroup(mdb_group);
            Logger.log("Joined Backup Channel at " + connectionInfo.getBackupChannelHostname() + ":" + connectionInfo.getBackupPort());
        } catch (IOException e) {
            return "IO ERROR";
        }

        //START LISTENING HERE
        Listener backupChannelListener = new BackupListener();
        Logger.log("Listening for backup messages...");
        backupChannelListener.listen();

        return "";
    }

    private String startRestoreChannelListener() {
        try {

            //Connect to Recovery Channel
            MulticastSocket socket = new MulticastSocket(connectionInfo.getRecoveryPort());

            connectionInfo.setRecoveryChannelCommunicator(new Communicator(socket));
            InetAddress mdr_group = InetAddress.getByName(connectionInfo.getRecoveryChannelHostname());
            socket.joinGroup(mdr_group);
            Logger.log("Joined Recovery Channel at " + connectionInfo.getRecoveryChannelHostname() + ":" + connectionInfo.getRecoveryPort());

        } catch (IOException e) {
            return "IO ERROR";
        }

        //START LISTENING HERE
        Listener recoveryChannelListener = new RecoveryListener();
        Logger.log("Listening for recovery messages...");
        recoveryChannelListener.listen();
        return "";
    }


    public void close() {

        System.out.println("Closing Peer, need to close sockets also, please implement me!");

        try {
            this.reg.unbind(this.rmi_name);
            UnicastRemoteObject.unexportObject(this.peer_remote_object,true);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

        Logger.log("Peer Disconnected. Press Enter to exit.");
        new java.util.Scanner(System.in).nextLine();
    }

    public void backup(String filePath, int replicationDegree) {

        try {
            Path path = Paths.get(filePath);

            String fileId = FileManager.calcFileId(path);

            Logger.log("Received Backup Request :");
            Logger.log("File Name: " + path.getFileName().toString());
            Logger.log("File ID: " + fileId);


            processFile(fileId, path, replicationDegree);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processFile(String fileId, Path path, int replicationDegree) {

        try {
            File file = path.toFile();
            FileInputStream file_stream = new FileInputStream(file);

            ChunkInfoStorer.getInstance().addPathToFileId(path.toString(), fileId);

            byte[] chunk = new byte[CHUNK_SIZE];
            int chunkLen = 0;
            int chunkNo = -1;
            while ((chunkLen = file_stream.read(chunk)) != -1) {
                chunkNo++;
                if(chunkLen == -1) {
                    putchunk(fileId, new byte[0], chunkNo, replicationDegree);
                    break;
                }

                //In case of last chunk, the chunk needs to be re-created,
                // because the read will only update the n first values
                if(chunkLen < CHUNK_SIZE) {

                    byte[] lastChunk = Arrays.copyOf(chunk, chunkLen);

                    putchunk(fileId, lastChunk, chunkNo, replicationDegree);
                    break;
                }

                byte[] chunkToSend = Arrays.copyOf(chunk, chunkLen);
                putchunk(fileId, chunkToSend, chunkNo, replicationDegree);


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void putchunk(String fileId, byte[] chunk, int chunkNo, int replicationDegree) {

        ChunkInfoStorer.getInstance().getChunkInfo(fileId, chunkNo).setDesiredReplicationDegree(replicationDegree);

        PutchunkHandler handler = new PutchunkHandler(fileId, chunkNo, replicationDegree, chunk);

        handler.send();


    }

    public void recover(String filePath) {
        GetchunkHandler handler = new GetchunkHandler(filePath);
        getchunkHandlers.put(handler.getFileId(), handler);
        handler.run();
    }

    public void recover_enhanced(String filePath) {
        GetchunkHandler handler = new GetchunkHandler(filePath, true);
        getchunkHandlers.put(handler.getFileId(), handler);
        handler.run();
    }

    public void delete(String filePath) {
        DeleteHandler handler = new DeleteHandler(filePath);
        handler.run();
    }

    public void reallocateSpace(int newSizeKB) {

        ALLOCATED_SPACE_KB = newSizeKB;

        for(ChunkKey key : ChunkInfoStorer.getInstance().getChunksToRemoveForNewSpace(ALLOCATED_SPACE_KB)){
            ChunkInfoStorer.getInstance().getChunkInfo(key.fileId, key.chunkNo).removePeer(Peer.PEER_ID);
            FileManager.removeChunk(key.fileId, key.chunkNo);

            RemovedHandler handler = new RemovedHandler(key.fileId);
            handler.run(key.chunkNo);
        }
    }

    public String getState(){
        return "State Information of Peer " + Peer.PEER_ID + ":\n\n Total Allocated Storage Space: " + this.ALLOCATED_SPACE_KB + " KB\n + Free Space Available: " + getFreeSpace() + " KB\n\n" + ChunkInfoStorer.getInstance().getInfo();
    }

    public ConcurrentHashMap<TaskLogKey, ChunkInfo> getTasks() {
        return tasks;
    }

    public PeerConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    public ScheduledExecutorService getThreadPool() {
        return threadPool;
    }

    public ConcurrentHashMap<TaskLogKey, ScheduledFuture> getTaskFutures() {
        return taskFutures;
    }

    public GetchunkHandler getChunkHandlerForFile(String fileId) {
        return getchunkHandlers.get(fileId);
    }

    public void removeGetchunkHandler(String fileId) {
        getchunkHandlers.remove(fileId);
    }

    public boolean isCompatible(String s) {
        return this.compatibleProtocolVersions.contains(s);
    }

    public int getFreeSpace() {
        return this.ALLOCATED_SPACE_KB - ChunkInfoStorer.getInstance().getUsedBytes() / 1000;
    }
}
