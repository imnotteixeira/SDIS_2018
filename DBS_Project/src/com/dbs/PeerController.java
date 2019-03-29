package com.dbs;

import com.dbs.filemanager.FileManager;
import com.dbs.messages.PutchunkMessage;
import com.dbs.utils.ByteToHex;
import com.dbs.utils.NetworkAddress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.concurrent.TimeUnit;

public class PeerController {

    private String rmi_name;
    PeerConnectionInfo connectionInfo;
    private final String BACKUP_DIR;

    private Registry reg = null;
    private PeerRemoteObject peer_remote_object = null;



    private final int CHUNK_SIZE = 5;


    public PeerController(String version, String peer_id, String rmi_name, String mc_address, String mdb_address, String mdr_address) {

        this.rmi_name = rmi_name;

        NetworkAddress mc_address_addr = new NetworkAddress(mc_address);
        NetworkAddress mdb_address_addr = new NetworkAddress(mdb_address);
        NetworkAddress mdr_address_addr = new NetworkAddress(mdr_address);



        this.BACKUP_DIR = "./peer_backup_"+ peer_id;

        this.connectionInfo = new PeerConnectionInfo(
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

        log("Starting Peer...");

        FileManager fm = new FileManager();
        log("Initialized File Manager.");

        initializeRemoteObject(fm);
        log("Initialized Remote Object.");



        log("Peer Ready.");

        Storer storerController = new Storer(
            this.connectionInfo
        );

        log("Listening for control messages...");
        storerController.run();

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

        try {
            //Connect to Control Channel
            this.connectionInfo.setControlChannelSocket(new MulticastSocket(this.connectionInfo.getControlPort()));
            InetAddress mc_group = InetAddress.getByName(this.connectionInfo.getControlChannelHostname());
            this.connectionInfo.getControlChannelSocket().joinGroup(mc_group);
            log("Joined Control Channel at " + this.connectionInfo.getControlChannelHostname() + ":" + this.connectionInfo.getControlPort());

            //Connect to Backup Channel
            this.connectionInfo.setBackupChannelSocket(new MulticastSocket(this.connectionInfo.getBackupPort()));
            InetAddress mdb_group = InetAddress.getByName(this.connectionInfo.getBackupChannelHostname());
            this.connectionInfo.getBackupChannelSocket().joinGroup(mdb_group);
            log("Joined Backup Channel at " + this.connectionInfo.getBackupChannelHostname() + ":" + this.connectionInfo.getBackupPort());

            //Connect to Recovery Channel
            this.connectionInfo.setRecoveryChannelSocket(new MulticastSocket(this.connectionInfo.getRecoveryPort()));
            InetAddress mdr_group = InetAddress.getByName(this.connectionInfo.getRecoveryChannelHostname());
            this.connectionInfo.getRecoveryChannelSocket().joinGroup(mdr_group);
            log("Joined Recovery Channel at " + this.connectionInfo.getRecoveryChannelHostname() + ":" + this.connectionInfo.getRecoveryPort());


        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public void close() {
        try {
            this.reg.unbind(this.rmi_name);
            UnicastRemoteObject.unexportObject(this.peer_remote_object,true);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }

        log("Peer Disconnected. Press Enter to exit.");
        new java.util.Scanner(System.in).nextLine();
    }

    public void backup(String filePath, int replicationDegree) {

        try {
            Path path = Paths.get(filePath);

            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);

            String fileId_raw = path.getFileName().toString() + attr.creationTime().toString();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fileId_hash = digest.digest(fileId_raw.getBytes(StandardCharsets.UTF_8));

            String fileId = ByteToHex.convert(fileId_hash);

            System.out.println("Saving File...");
            System.out.println("File Name: " + path.getFileName().toString());
            System.out.println("Creation Time: " + attr.creationTime());
            System.out.println("File ID: " + fileId);


            processFile(fileId, path, replicationDegree);


        } catch (IOException | NoSuchAlgorithmException e) {
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
                putchunk(fileId, chunk, chunkNo, replicationDegree);


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void putchunk(String fileId, byte[] chunk, int chunkNo, int replicationDegree) {
        System.out.println("Sending PUTCHUNK");

        PutchunkMessage msg = new PutchunkMessage(this.connectionInfo.getVersion(), String.valueOf(this.connectionInfo.getSenderId()), fileId, String.valueOf(chunkNo), String.valueOf(replicationDegree), chunk);

        msg.send(connectionInfo.getBackupChannelSocket(), connectionInfo.getBackupChannelHostname(), connectionInfo.getBackupPort());


    }

    public void log(String msg) {
        System.out.println("[" + this.connectionInfo.getSenderId() + "] " + msg);
    }

    public String getBackupDir() {
        return BACKUP_DIR;
    }
}
