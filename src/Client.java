import Inet.PackageData;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    static Scanner scanner = new Scanner(System.in);

    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;
    private final String server;
    private String userName;
    private final int port;

    public Client(String server, int port, String userName) {
        this.server = server;
        this.userName = userName;
        this.port = port;
    }

    public boolean start(){
        try{
            socket = new Socket(server, port);
        }catch(Exception e){
            showSystemMessage("Error connecting to server: " + e);
            return false;
        }

        String message = "Connection accepted " + socket.getInetAddress() + " : " + socket.getPort();
        showSystemMessage(message);

        try{
            // 3 hours for this
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        }catch(IOException e){
            return false;
        }
        try{
            sOutput.writeObject(userName);
        }catch(IOException e){
            showSystemMessage("Exception performing login " + e);
            disconnect();
            return false;
        }
        return true;
    }

    public static void main(String[] args) {

        int portNumber = 1500;
        String serverAddress = "localhost";

        System.out.println("Enter User Name: ");
        String userName = scanner.nextLine();

        switch(args.length){
            case 3:
                serverAddress = args[2];
            case 2:
                try{
                    portNumber = Integer.parseInt(args[1]);
                }catch (Exception e){
                    System.out.println("Invalid port number. ");
                    System.out.println("Usage is: > Java Client [username] [portNumber] [serverAddress]");
                    return;
                }
            case 1:
                userName = args[0];
            case 0:
                break;
            default:
                System.out.println("Usage is: > Java Client [username] [portNumber] [serverAddress]");
                return;
        }

        Client client = new Client(serverAddress, portNumber, userName);

        if (!client.start()) {
            return;
        }

        showSystemMessage("All good!");

        boolean flag = true;
        while(flag){
            var data = client.receive();
            switch (data.getType()) {
                case Bet:
                    var bet = getBet(data.getMoney());
                    client.send(bet);
                    break;
                case Message:
                    System.out.println(data.getMessage());
                    break;
                case Finish:
                    flag = false;
                    break;
            }
        }

        scanner.close();
        client.disconnect();

    }

    private static int getBet(int startMoney) {
        System.out.println("[Test] Your money:" + startMoney);
        System.out.println("[Test] Your bet: 1500");
        return 1500;
    }

    private PackageData receive() {
        PackageData data = null;
        try{
            data = (PackageData) sInput.readObject();
        } catch(IOException e) {
            showSystemMessage("Server has closed the connection: " + e);
        } catch(ClassNotFoundException ignored) { }

        return data;
    }

    private <TData> void send(TData data) {
        try {
            sOutput.writeObject(data);
        } catch (IOException e) {
            showSystemMessage("Server has closed the connection: " + e);
        }
    }

    private void disconnect(){
        try{
            if (sInput != null) {
                sInput.close();
            }
        }catch (Exception e){}
        try{
            if (sOutput != null) {
                sOutput.close();
            }
        }catch (Exception e){}
        try{
            if (socket != null) {
                socket.close();
            }
        }catch (Exception e){}
    }

    public static void showSystemMessage(String message) {
        System.out.println("*** " + message + " ***");
    }
}
