package com.dbs;

public class PeerConnectionInfo {

    private int senderId, controlPort, backupPort, recoveryPort;
    private byte[] version;
    private String controlChannelHostname, backupChannelHostname, recoveryChannelHostname;

    private Communicator controlChannelCommunicator, backupChannelCommunicator, recoveryChannelCommunicator;

    public PeerConnectionInfo(int senderId, byte[] version, String controlChannelHostname, int controlPort, String backupChannelHostname, int backupPort, String recoveryChannelHostname, int recoveryPort) {
        this.senderId = senderId;
        this.version = version;
        this.controlChannelHostname = controlChannelHostname;
        this.controlPort = controlPort;
        this.backupChannelHostname = backupChannelHostname;
        this.backupPort = backupPort;
        this.recoveryChannelHostname = recoveryChannelHostname;
        this.recoveryPort = recoveryPort;
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

    public Communicator getControlChannelCommunicator() {
        return controlChannelCommunicator;
    }

    public Communicator getBackupChannelCommunicator() {
        return backupChannelCommunicator;
    }

    public Communicator getRecoveryChannelCommunicator() {
        return recoveryChannelCommunicator;
    }


    public void setControlChannelCommunicator(Communicator controlChannelCommunicator) {
        this.controlChannelCommunicator = controlChannelCommunicator;
    }

    public void setBackupChannelCommunicator(Communicator backupChannelCommunicator) {
        this.backupChannelCommunicator = backupChannelCommunicator;
    }

    public void setRecoveryChannelCommunicator(Communicator recoveryChannelCommunicator) {
        this.recoveryChannelCommunicator = recoveryChannelCommunicator;
    }
}
