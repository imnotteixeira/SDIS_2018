package com.dbs.messages;

import com.dbs.Communicator;
import com.dbs.PeerController;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RemovedMessage extends ChunkDependentMessage {

    public RemovedMessage(byte[] version, String senderId, String fileId, String chunkNo) {
        super("REMOVED", version, senderId, fileId, chunkNo);
    }

    @Override
    protected String getHostname() {return PeerController.connectionInfo.getControlChannelHostname();}

    @Override
    protected int getPort() {return PeerController.connectionInfo.getControlPort();}

    @Override
    protected Communicator getCommunicator() { return PeerController.connectionInfo.getControlChannelCommunicator(); }

    public static RemovedMessage fromString(byte[] src) throws IllegalStateException{

        Pattern r = Pattern.compile("(REMOVED)\\s+(\\d\\.\\d)\\s+(\\d+)\\s+(\\w+)\\s+(\\d{1,6})\\s+" +
                PeerMessage.CRLF +
                PeerMessage.CRLF
        );

        Matcher m = r.matcher(new String(src, 0, src.length));

        m.find();

        byte[] version = m.group(2).getBytes();
        String senderId = m.group(3);
        String fileId = m.group(4);
        String chunkNo = m.group(5);


        return new RemovedMessage(version, senderId, fileId, chunkNo);
    }
}
