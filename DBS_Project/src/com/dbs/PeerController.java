package com.dbs;

import com.dbs.filemanager.FileManager;
import com.dbs.handlers.PutchunkHandler;
import com.dbs.listeners.BackupListener;
import com.dbs.listeners.ControlListener;
import com.dbs.listeners.Listener;
import com.dbs.listeners.RecoveryListener;
import com.dbs.messages.ChunkMessage;
import com.dbs.messages.GetchunkMessage;
import com.dbs.utils.ByteToHex;
import com.dbs.utils.Logger;
import com.dbs.utils.NetworkAddress;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.*;

public class PeerController {

    private final int CHUNK_SIZE = (int) 64e3;
//    private final int CHUNK_SIZE = 5;
    private String rmi_name;
    public static PeerConnectionInfo connectionInfo;
    private final String BACKUP_DIR;

    private Registry reg = null;
    private PeerRemoteObject peer_remote_object = null;

    private ConcurrentHashMap<TaskLogKey, ChunkInfo> tasks = new ConcurrentHashMap<>();
    private ConcurrentHashMap<TaskLogKey, ScheduledFuture> taskFutures = new ConcurrentHashMap<>();

    private ScheduledExecutorService threadPool;


    private static PeerController instance = null;

       // The static method that controls access to the singleton
    // instance.
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

        NetworkAddress mc_address_addr = new NetworkAddress(mc_address);
        NetworkAddress mdb_address_addr = new NetworkAddress(mdb_address);
        NetworkAddress mdr_address_addr = new NetworkAddress(mdr_address);



        this.BACKUP_DIR = "./peer_backup_"+ peer_id;

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

        FileManager fm = new FileManager();
        Logger.log("Initialized File Manager.");

        initializeRemoteObject(fm);
        Logger.log("Initialized Remote Object.");


        Logger.log("Peer Ready.");

    }

    public void initializeRemoteObject(FileManager fm) {
        this.peer_remote_object = new PeerRemoteObject(this, fm);
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

        TaskLogKey key = new TaskLogKey(fileId, chunkNo, TaskType.STORE);
        ChunkInfo value = new ChunkInfo(new HashSet<>(), replicationDegree);
        PeerController.getInstance().getTasks().put(key, value);

        PutchunkHandler handler = new PutchunkHandler(fileId, chunkNo, replicationDegree, chunk);
        handler.send();

    }

    public boolean replicationDegreeReached(String fileId, int chunkNo, TaskType taskType) {
        TaskLogKey key = new TaskLogKey(fileId, chunkNo, taskType);
        ChunkInfo chunkInfo = this.tasks.get(key);

        return chunkInfo.peers.size() >= chunkInfo.desiredReplication;
    }

    public void recover(String filePath) {
        try {
            String fileId = FileManager.calcFileId(Paths.get(filePath));
            getchunk(fileId, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getchunk(String fileId, int chunkNo) {
        GetchunkMessage msg = new GetchunkMessage(
                Peer.VERSION.getBytes(),
                Peer.PEER_ID,
                fileId,
                String.valueOf(chunkNo)
        );

        tasks.put(new TaskLogKey(fileId, chunkNo, TaskType.CHUNK), new ChunkInfo());

        msg.send();
    }

    public void processReceivedChunk(ChunkMessage msg) {

        TaskLogKey key = new TaskLogKey(msg.getFileId(), Integer.parseInt(msg.getChunkNo()), TaskType.CHUNK);

        if(tasks.containsKey(key)){

            tasks.remove(key);

            Logger.log("Received CHUNK for file " + msg.getFileId() + " with number " + msg.getChunkNo());

            //Append to output file here !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! !!!!!    !!!!!

            if(msg.getBody().length == CHUNK_SIZE){
                getchunk(msg.getFileId(), Integer.parseInt(msg.getChunkNo()) + 1);
            }

        }
        //was not waiting for this chunk
    }

    public String getBackupDir() {
        return BACKUP_DIR;
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
}
