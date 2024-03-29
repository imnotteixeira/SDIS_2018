package com.dbs.messages;

import com.dbs.Communicator;
import com.dbs.PeerController;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

    public class StoredMessage extends ChunkDependentMessage {

    public StoredMessage(byte[] version, String senderId, String fileId, String chunkNo) {
        super("STORED", version, senderId, fileId, chunkNo);
    }

    @Override
    protected String getHostname() {return PeerController.connectionInfo.getControlChannelHostname();}

    @Override
    protected int getPort() {return PeerController.connectionInfo.getControlPort();}

    @Override
    protected Communicator getCommunicator() { return PeerController.connectionInfo.getControlChannelCommunicator(); }


    public static StoredMessage fromString(byte[] src) throws IllegalStateException {

        Pattern r = Pattern.compile("(STORED)\\s+(\\d\\.\\d)\\s+(\\d+)\\s+(\\w+)\\s+(\\d{1,6})\\s+" +
                PeerMessage.CRLF +
                PeerMessage.CRLF
        );

        Matcher m = r.matcher(new String(src, 0, src.length));
        m.find();

        byte[] version = m.group(2).getBytes();
        String senderId = m.group(3);
        String fileId = m.group(4);
        String chunkNo = m.group(5);


        return new StoredMessage(version, senderId, fileId, chunkNo);
    }
}
