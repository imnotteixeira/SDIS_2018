package com.dbs;

import java.net.MulticastSocket;

public class PeerConnectionInfo {

    private int senderId;
    private byte[] version;

    private MulticastSocket controlChanelSocket, backupChannelSocket, recoveryChannelSocket;

    public PeerConnectionInfo(int senderId, byte[] version, MulticastSocket controlChanelSocket, MulticastSocket backupChannelSocket, MulticastSocket recoveryChannelSocket) {
        this.senderId = senderId;
        this.version = version;
        this.controlChanelSocket = controlChanelSocket;
        this.backupChannelSocket = backupChannelSocket;
        this.recoveryChannelSocket = recoveryChannelSocket;
    }

    public int getSenderId() {
        return senderId;
    }

    public byte[] getVersion() {
        return version;
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
