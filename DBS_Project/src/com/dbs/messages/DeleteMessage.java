package com.dbs.messages;

import com.dbs.PeerController;
import com.dbs.utils.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeleteMessage extends PeerMessage {

    private int headerSize;

    public DeleteMessage(byte[] version, String senderId, String fileId) {
        super("DELETE", version, senderId, fileId);

        this.headerSize = this.toString().getBytes().length + PeerMessage.CRLF.getBytes().length * 2;
    }

    @Override
    public String toString() {
        return super.toString() + " ";
    }

    public byte[] getByteMsg() {
        return ByteBuffer.allocate(this.headerSize)
                .put(toString().getBytes())
                .put(PeerMessage.CRLF.getBytes())
                .put(PeerMessage.CRLF.getBytes()).array();
    }

    @Override
    public void send() {

        String hostname = PeerController.connectionInfo.getControlChannelHostname();
        int port = PeerController.connectionInfo.getControlPort();

        final byte[] msg = getByteMsg();

        try {
            PeerController.getInstance().getConnectionInfo().getControlChannelCommunicator().send(msg, hostname, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DeleteMessage fromString(byte[] src) throws IllegalStateException{

        Pattern r = Pattern.compile("(DELETE)\\s+(\\d\\.\\d)\\s+(\\d+)\\s+(.+)\\s+" +
                PeerMessage.CRLF +
                PeerMessage.CRLF
        );

        Matcher m = r.matcher(new String(src, 0, src.length));

        m.find();


        byte[] version = m.group(2).getBytes();
        String senderId = m.group(3);
        String fileId = m.group(4);


        return new DeleteMessage(version, senderId, fileId);
    }
}
