package com.dbs;

import java.net.MulticastSocket;

public class PeerConnectionInfo {

    private int senderId, controlPort, backupPort, recoveryPort;
    private byte[] version;
    private String controlChannelHostname, backupChannelHostname, recoveryChannelHostname;

    private MulticastSocket controlChannelSocket, backupChannelSocket, recoveryChannelSocket;

    public PeerConnectionInfo(int senderId, byte[] version, String controlChannelHostname, int controlPort, String backupChannelHostname, int backupPort, String recoveryChannelHostname, int recoveryPort, MulticastSocket controlChanelSocket, MulticastSocket backupChannelSocket, MulticastSocket recoveryChannelSocket) {
        this.senderId = senderId;
        this.version = version;
        this.controlChannelHostname = controlChannelHostname;
        this.controlPort = controlPort;
        this.backupChannelHostname = backupChannelHostname;
        this.backupPort = backupPort;
        this.recoveryChannelHostname = recoveryChannelHostname;
        this.recoveryPort = recoveryPort;

        this.controlChannelSocket = controlChanelSocket;
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

    public String getControlChannelHostname() {
        return controlChannelHostname;
    }

    public String getBackupChannelHostname() {
        return backupChannelHostname;
    }

    public String getRecoveryChannelHostname() {
        return recoveryChannelHostname;
    }

    public MulticastSocket getControlChannelSocket() {
        return controlChannelSocket;
    }

    public MulticastSocket getBackupChannelSocket() {
        return backupChannelSocket;
    }

    public MulticastSocket getRecoveryChannelSocket() {
        return recoveryChannelSocket;
    }


}
