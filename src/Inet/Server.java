package Inet;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Server {

    public Server(int port) {
        this.al = new ArrayList<>();
        this.sdf = new SimpleDateFormat("HH:mm:ss");
        this.port = port;
    }

    public void start(){
        keepGoing = true;
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while(keepGoing){
                display("Server waiting for Clients on port " + port + ".");
                Socket socket = serverSocket.accept();
                if (!keepGoing) //?
                    break;
                ClientThread thread = new ClientThread(socket);
                al.add(thread);
                thread.start();
            }
            try{
                serverSocket.close();
                for (ClientThread tc : al) {
                    try {
                        tc.sInput.close();
                        tc.sOutput.close();
                        tc.socket.close();
                    } catch (IOException ioE) {
                        ioE.printStackTrace();
                    }
                }
            }catch(Exception e){
                display("Exception closing the server and clients: " + e);
            }
        }catch (IOException e){
            String message = sdf.format(new Date() + " Exception on new ServerSocket: " + e + "\n");
            display(message);
        }
    }
}
