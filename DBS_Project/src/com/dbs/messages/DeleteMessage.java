package com.dbs.messages;

import java.net.MulticastSocket;

public class DeleteMessage extends PeerMessage {
    public DeleteMessage(byte[] version, String senderId, String fileId) {
        super("DELETE", version, senderId, fileId);
    }

    @Override
    public void send(MulticastSocket socket, String host, int port) {

    }
}
