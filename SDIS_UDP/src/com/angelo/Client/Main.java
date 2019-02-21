import java.io.IOException;
import java.net.PortUnreachableException;
import java.net.SocketTimeoutException;

public class Main {

    public static void main(String[] args) {

        final int N_ATTEMPTS = 10;

        System.out.println(args[0]);
        SocketClient client;
        try {
             client = new SocketClient("10.227.145.8");
        }catch(Exception e){
            System.out.println("ERROR OPENING SOCKET");
            e.printStackTrace();
            return;
        }

        for(int i = 0; i < N_ATTEMPTS; i++) {
            System.out.println("Attempt nbr " + (i+1));
            try {
                client.sendMessage(args[0]);
            }catch(Exception e){
                System.out.println("ERROR SENDING REQUEST");
                e.printStackTrace();
            }
            try{
                System.out.println(client.getResponse());
                break;
            }catch(PortUnreachableException e){
                System.out.println("Unable to establish connection");
            }catch(SocketTimeoutException e){
                System.out.println("Request timed out!");
            }catch(IOException e){
                e.printStackTrace();
            }
        }


    }


}
