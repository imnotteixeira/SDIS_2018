package com.dbs;

import com.dbs.filemanager.FileManager;
import com.dbs.utils.NetworkAddress;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class PeerController {

    private final String version;
    private final String peer_id;
    private final String rmi_name;
    private final NetworkAddress mc_address;
    private final NetworkAddress mdb_address;
    private final NetworkAddress mdr_address;
    private final String BACKUP_DIR;

    private Registry reg = null;
    private PeerRemoteObject peer_remote_object = null;

    private MulticastSocket mc_socket;
    private MulticastSocket mdb_socket;
    private MulticastSocket mdr_socket;


    public PeerController(String version, String peer_id, String rmi_name, String mc_address, String mdb_address, String mdr_address) {
        this.version = version;
        this.peer_id = peer_id;
        this.rmi_name = rmi_name;
        this.mc_address = new NetworkAddress(mc_address);
        this.mdb_address = new NetworkAddress(mdb_address);
        this.mdr_address = new NetworkAddress(mdr_address);

        this.BACKUP_DIR = "./peer_backup_"+ peer_id;
    }

    public void start() {

        log("Starting Peer...");

        FileManager fm = new FileManager();
        log("Initialized File Manager.");

        initializeRemoteObject(fm);
        log("Initialized Remote Object.");

        initializePeerNetworkConnection();

        log("Peer Ready.");

        Storer storerController = new Storer(
            new PeerConnectionInfo(
                Integer.valueOf(this.peer_id),
                this.version.getBytes(),
                this.mc_address.hostname,
                this.mc_address.port,
                this.mdb_address.hostname,
                this.mdb_address.port,
                this.mdr_address.hostname,
                this.mdr_address.port,
                this.mc_socket,
                this.mdb_socket,
                this.mdr_socket
            )
        );

        log("Listening for control messages...");
        storerController.run();

    }


    public void initializeRemoteObject(FileManager fm) {
        this.peer_remote_object = new PeerRemoteObject(fm, this.BACKUP_DIR);
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
            this.mc_socket = new MulticastSocket(this.mc_address.port);
            InetAddress mc_group = InetAddress.getByName(this.mc_address.hostname);
            this.mc_socket.joinGroup(mc_group);
            log("Joined Control Channel at " + this.mc_address.hostname + ":" + this.mc_address.port);

            //Connect to Backup Channel
            this.mdb_socket = new MulticastSocket(this.mdb_address.port);
            InetAddress mdb_group = InetAddress.getByName(this.mdb_address.hostname);
            this.mdb_socket.joinGroup(mdb_group);
            log("Joined Backup Channel at " + this.mdb_address.hostname + ":" + this.mdb_address.port);

            //Connect to Recovery Channel
            this.mdr_socket = new MulticastSocket(this.mdr_address.port);
            InetAddress mdr_group = InetAddress.getByName(this.mdr_address.hostname);
            this.mdr_socket.joinGroup(mdr_group);
            log("Joined Recovery Channel at " + this.mdr_address.hostname + ":" + this.mdr_address.port);


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

    public void log(String msg) {
        System.out.println("[" + this.peer_id + "] " + msg);
    }
}
