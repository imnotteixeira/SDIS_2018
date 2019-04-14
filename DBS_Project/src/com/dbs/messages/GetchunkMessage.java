package com.dbs.messages;

import com.dbs.Communicator;
import com.dbs.PeerController;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetchunkMessage extends ChunkDependentMessage {

    public GetchunkMessage(byte[] version, String senderId, String fileId, String chunkNo) {
        super("GETCHUNK", version, senderId, fileId, chunkNo);
    }

    @Override
    public String getHostname(){ return PeerController.connectionInfo.getControlChannelHostname(); }

    @Override
    public int getPort(){ return PeerController.connectionInfo.getControlPort(); }

    @Override
    protected Communicator getCommunicator() { return PeerController.connectionInfo.getControlChannelCommunicator(); }

    public static GetchunkMessage fromString(byte[] src) throws IllegalStateException{

        Pattern r = Pattern.compile("(GETCHUNK)\\s+(\\d\\.\\d)\\s+(\\d+)\\s+(\\w+)\\s+(\\d{1,6})\\s+" +
                PeerMessage.CRLF +
                PeerMessage.CRLF
        );

        Matcher m = r.matcher(new String(src, 0, src.length));

        m.find();


        byte[] version = m.group(2).getBytes();
        String senderId = m.group(3);
        String fileId = m.group(4);
        String chunkNo = m.group(5);


        return new GetchunkMessage(version, senderId, fileId, chunkNo);
    }
}
