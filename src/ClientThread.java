import Cards.Card;
import Inet.PackageData;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ClientThread {
    private Socket socket;
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;

    private int money;
    private String username;
    private ArrayList<Card> cards;

    public boolean bustFlag;

    public ClientThread (Socket socket){
        this.socket = socket;
        this.money = 5000;
        this.cards = new ArrayList<>();
        this.bustFlag = false;

        try {
            this.sOutput = new ObjectOutputStream(socket.getOutputStream());
            this.sInput = new ObjectInputStream(socket.getInputStream());
            this.username = (String) sInput.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Server.showSystemMessage("Cannot create client");
        }
    }

    public void addCard(Card card) {
        cards.add(card);
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public String getUsername() {
        return username;
    }

    public void addMoney(int money){
        this.money += money;
    }

    public void removeMoney(int money){
        this.money -= money;
    }

    public <TType> TType receive() {
        try {
            return  (TType) sInput.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Server.showSystemMessage("Error in receiving data from client");
            return null;
        }
    }

    public void send(PackageData data) {
        if(!socket.isConnected()) {
            close();
        }
        try {
            sOutput.writeObject(data);
        }
        catch(IOException e) {
            Server.showSystemMessage("Error in sending package to " + username);
        }
    }


    public void close(){
        try{
            if (sOutput != null) {
                sOutput.close();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        try{
            if (sInput!= null) {
                sInput.close();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
        try{
            if (socket!= null) {
                socket.close();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

}
