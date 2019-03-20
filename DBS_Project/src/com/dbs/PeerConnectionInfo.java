package com.dbs;

import java.net.MulticastSocket;

public class PeerConnectionInfo {

    private int senderId, controlPort, backupPort, recoveryPort;
    private byte[] version;
    String multicastHostname;

    private MulticastSocket controlChanelSocket, backupChannelSocket, recoveryChannelSocket;

    public PeerConnectionInfo(int senderId, byte[] version, String multicastHostname, int controlPort, int backupPort, int recoveryPort, MulticastSocket controlChanelSocket, MulticastSocket backupChannelSocket, MulticastSocket recoveryChannelSocket) {
        this.senderId = senderId;
        this.controlPort = controlPort;
        this.backupPort = backupPort;
        this.recoveryPort = recoveryPort;
        this.version = version;
        this.multicastHostname = multicastHostname;
        this.controlChanelSocket = controlChanelSocket;
        this.backupChannelSocket = backupChannelSocket;
        this.recoveryChannelSocket = recoveryChannelSocket;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getControlPort() {
        return controlPort;
    }

    public int getBackupPort() {
        return backupPort;
    }

    public int getRecoveryPort() {
        return recoveryPort;
    }

    public byte[] getVersion() {
        return version;
    }

    public String getMulticastHostname() {
        return multicastHostname;
    }

    public MulticastSocket getControlChanelSocket() {
        return controlChanelSocket;
    }

    public MulticastSocket getBackupChannelSocket() {
        return backupChannelSocket;
    }

    public MulticastSocket getRecoveryChannelSocket() {
        return recoveryChannelSocket;
    }
}
