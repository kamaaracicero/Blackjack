package Inet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientThread {
    Socket socket;
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;

    PackageData data;
    public ClientThread (Socket socket){
        this.socket = socket;
        try {
            this.sInput = new ObjectInputStream(socket.getInputStream());
            this.sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException ex) {

        }
    }

    public void getMessage() {
        try {
            data = (PackageData) sInput.readObject();
        } catch (IOException | ClassNotFoundException ex) {

        }
    }
}
