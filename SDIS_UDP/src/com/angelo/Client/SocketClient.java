import java.io.IOException;
import java.net.*;

public class SocketClient {

    private final String serverHostname;
    private DatagramSocket datagramSocket;
    private final int port = 8765;
    private static final int TIMEOUT = 10000;
    private static final int BUF_SIZE = 512;

    SocketClient(String serverHostname) throws SocketException {
        this.serverHostname = serverHostname;
        datagramSocket = new DatagramSocket();
        datagramSocket.setSoTimeout(SocketClient.TIMEOUT);
        System.out.println("Opened socket connected to hostname " + serverHostname + " on port " + this.port);
    }

    void sendMessage(String message) throws IOException {
        byte[] buf = message.getBytes();

        DatagramPacket packet = new DatagramPacket(buf, buf.length,
                InetAddress.getByName(this.serverHostname), this.port);
        this.datagramSocket.send(packet);
    }

    String getResponse() throws IOException{
        byte[] buf = new byte[256];
        DatagramPacket response = new DatagramPacket(buf, SocketClient.BUF_SIZE);
        this.datagramSocket.receive(response);
        return new String(response.getData(), 0, response.getLength());
    }

}
