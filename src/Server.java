import CardGenerators.CardGenerator;
import CardGenerators.DefaultGenerator;
import Cards.Card;
import GameRules.DefaultRules;
import Inet.PackageData;
import Inet.PackageDataType;
import StringBuilders.CardsStringBuilder;
import StringBuilders.StandardBuilder;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    static Scanner sc = new Scanner(System.in);

    static Dealer dealer;

    static ArrayList<Card> deck;

    static CardGenerator cardGenerator = new DefaultGenerator();
    CardsStringBuilder strBuilder = new StandardBuilder();
    DefaultRules rules = new DefaultRules();

    static Dictionary<String, Integer> bets;

    final static int cardLimit = 22;

    private final ArrayList<ClientThread> al;

    private final int port;

    private String winner;

    public Server(int port) {
        al = new ArrayList<>();
        this.port = port;
    }

    public void game() {
        boolean playCondition = false;
        boolean blackjackThing = false;
        do {
            deckCreation();
            makingBets();
            dealingCards();
            showingCards();
            blackjackThing = checkBlackjack();

            if (!blackjackThing){
                hitOrStand();
                //dealerGetsCards();
                betClash();
                //clearingHands();
            }
            //playCondition = endingGame();
            playCondition = false;


        } while (playCondition);

    }

    public static void main(String[] args) {
        int portNumber = 1500;

        switch (args.length){
            case 1:
                try{
                    portNumber = Integer.parseInt(args[0]);
                }catch(Exception e){
                    System.out.println("Invalid port number.");
                    System.out.println("Usage is: > Java Server [portNumber]");
                    e.printStackTrace();
                    return;
                }
            case 0:
                break;
            default:
                System.out.println("Usage is: > Java Server [portNumber]");
                return;
        }
        Server server = new Server(portNumber);
        server.start();
    }

    public void start(){
        int fillOut = 0;
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            dealer = new Dealer();

            System.out.println("What's dealer's name?");
            dealer.setName(sc.next());

            System.out.println("How many people are playing (1-4)");
            int playercount;
            do {
                playercount = sc.nextInt();
            } while (playercount > 5 || playercount < 1);

            while(fillOut < playercount){
                Socket socket = serverSocket.accept();
                ClientThread thread = new ClientThread(socket);
                al.add(thread);
                ++fillOut;
            }

            //-------|
            game();
            //-------|

            try{
                serverSocket.close();
                for (ClientThread tc : al) {
                    tc.close();
                }
            }catch(Exception e){
                showSystemMessage("Exception closing the server and clients: " + e);
            }
        }catch (IOException e){
            showSystemMessage("Exception on new ServerSocket: " + e + "\n");
        }
    }

    public void deckCreation() {
        deck = cardGenerator.generateDeck();
    }

    public void makingBets() {
        bets = new Hashtable<>();
        PackageData data;
        for (int j = 0; j < al.size(); j++) {
            var player = al.get(j);
            data = new PackageData(PackageDataType.Bet, player.getMoney());

            player.send(data);
            int bet = player.<Integer>receive();

            bets.put(player.getUsername(), bet);
        }
    }

    public void dealingCards() {
        for (int i = 0; i < 2; i++) {
            dealer.addCard(getCardFromDeck());
        }
        for (int i = 0; i < al.size(); i++) {
            var client = al.get(i);
            for (int j = 0; j < 2; j++) {
                client.addCard(getCardFromDeck());
            }
        }
    }

    public Card getCardFromDeck() {
        int i = (int) (Math.random() * deck.size());
        Card card = deck.get(i);
        deck.remove(card);
        return card;
    }

    public void showingCards() {
        var dealersCard = dealer.getCards().get(1).toString();
        for (ClientThread client : al) {
            sendToClientCardsWithPoints(client);
            sendMessageToClient(client, ("Card of the dealer is " + dealersCard + "\n"));
        }
    }

    private void sendToClientCardsWithPoints(ClientThread client) {
        var str = strBuilder.getCardsString(client.getCards(), "Your cards:\n");
        var points = rules.cardCounter(client.getCards());

        sendMessageToClient(client, str);
        sendMessageToClient(client, ("Your points: " + points + "\n"));
    }

    private static void clientDrawBet(ClientThread client) {
        bets.remove(client.getUsername());
    }

    private static void clientWonBet(ClientThread client, int bet) {
        client.addMoney(bet);
        dealer.removeMoney(bet);
        bets.remove(client.getUsername());
    }

    private static void clientLoseBet(ClientThread client, int bet) {
        client.removeMoney(bet);
        dealer.addMoney(bet);
        bets.remove(client.getUsername());
    }

    private static void sendMessageToClient(ClientThread client, String message) {
        client.send(new PackageData(PackageDataType.Message, message));
    }

    public boolean checkBlackjack() {
        int dealerSum = rules.cardCounter(dealer.getCards());
        boolean condition = false;

        if (dealerSum == 21) {
            for (ClientThread client : al) {
                int playerSum = rules.cardCounter(client.getCards());
                int bet = bets.get(client.getUsername());

                if (playerSum != dealerSum) {
                    sendMessageToClient(client,
                            "The dealer has blackjack and you don't, that's a loss!\n");
                    clientLoseBet(client, bet);
                } else {
                    sendMessageToClient(client,
                            "The dealer has blackjack and you too, that's a push!\n");
                    clientDrawBet(client);
                }
                sendMessageToClient(client,
                        "Your money: " + client.getMoney() + "\n");
            }

            condition = true;
        } else {
            for (ClientThread client : al) {
                int playerSum = rules.cardCounter(client.getCards());
                int bet = bets.get(client.getUsername());

                if (playerSum == 21) {
                    sendToClientCardsWithPoints(client);
                    sendMessageToClient(client,
                            "You have a blackjack!");
                    clientWonBet(client, (int) (bet * 1.5));
                    client.bustFlag = true;
                }
            }
        }

        return condition;
    }

    //universal method
    /*
    public void showingPlayerCards(Person person) {
        var playerCards = person.getCards();

        System.out.println("Player " + person.getName() + "'s cards are: ");
        for (int j = 0; j < playerCards.size(); j++) {
            System.out.print(playerCards.get(j) + " ");
        }
    }

    //universal method
    public void showingDealersCards() {
        var dealerCards = dealer.getCards();

        System.out.println("Dealer " + dealer.getName() + "'s cards are: ");
        for (var card : dealerCards) {
            System.out.println(card + " ");
        }
    }
    */

    public void hitOrStand() {
        for (ClientThread client : al) {
            for (int i = 0; i < cardLimit; i++) {

                sendToClientCardsWithPoints(client);
                client.send(new PackageData(PackageDataType.HitOrStand));

                var choice = client.<String>receive();
                var bet = bets.get(client.getUsername());

                if (choice.equalsIgnoreCase("HIT")) {

                    client.addCard(getCardFromDeck());
                    int sumOfCards = rules.cardCounter(client.getCards());

                    if (sumOfCards > 21) {
                        sendToClientCardsWithPoints(client);
                        sendMessageToClient(client, "You lose!");
                        clientLoseBet(client, bet);
                        client.bustFlag = true;

                        break;
                    } else {
                        sendToClientCardsWithPoints(client);
                    }
                } else if (choice.equalsIgnoreCase("STAND")) {

                    break;
                }
            }
        }
    }
/*
    public void dealerGetsCards() {
        DefaultRules rules = new DefaultRules();
        var dealerCards = dealer.getCards();
        System.out.println("Dealer shows his cards, his cards are: ");
        showingDealersCards();

        for (int j = 0; j < cardLimit; j++) {
            int dealerSum = rules.cardCounter(dealerCards);

            if (dealerSum > 21) {
                System.out.println("Dealer busts!");
                break;
            } else if (dealerSum > 16 && dealerSum < 22) {
                System.out.println("Dealer stands, his cards are: ");
                showingDealersCards();
                break;
            } else {
                System.out.println("Dealer draws a card.");
                dealer.addCard(getCardFromDeck());
                System.out.println("Dealer's cards are: " + dealer.getCards());
            }
        }
    }
*/
    public void betClash() {
        DefaultRules rules = new DefaultRules();
        var dealerSum = rules.cardCounter(dealer.getCards());

        for (ClientThread client : al) {

            var playerSum = rules.cardCounter(client.getCards());
            var bet = bets.get(client.getUsername());

            if (dealerSum > 21) { // if dealer busts
                if (client.bustFlag) {

                } else if (playerSum > 21) { // if player busts at the same time
                    //System.out.println("Dealer gets the bet of the player " + client.getName() + " because the player busted");
                    clientLoseBet(client, bet);

                } else if (playerSum == 21) { // if player has blackjack
                    //System.out.println("Player " + client.getName() + " wins the bet because dealer busted!");
                    clientWonBet(client, bet);

                } else { // if player exists and is an actual functioning member of society playing blackjack (member of society that plays blackjack sure)
                    //System.out.println("Player " + client.getName() + " wins the bet because dealer busted! (bonus)");
                    clientWonBet(client, (int)(bet * 2));
                }

            } else {
                if (client.bustFlag) {

                } else if (playerSum < dealerSum) { //player's sum of cards is less than the dealer's
                    //System.out.println("Player " + client.getName() + " busts, dealer takes the bet.");
                    clientLoseBet(client, bet);
                } else if (playerSum > dealerSum) { // player's winning
                    //System.out.println("Player " + client.getName() + " wins the bet!");
                    clientWonBet(client, bet);
                } else { // both push (dealer's sum == player's sum)
                    //System.out.println("Both player " + client.getName() + " and dealer push (draw).");
                    clientDrawBet(client);
                }
            }
        }
    }
/*
    public void clearingHands () {
        //Clearing hands and showing money
        dealer.clearHand();
        for (int i = 0; i < people.size(); i++) {
            Person person = people.get(i);
            person.clearHand();
        }
        System.out.println("Dealer's money is " + dealer.getMoney());
        for (int i = 0; i < people.size(); i++) {
            Person person = people.get(i);
            System.out.println(person.getName() + "'s money is " + person.getMoney());
        }

    }

    public boolean endingGame() {
        //the end?
        System.out.println("Do you want to continue to play? (yes/no)");
        String answer = sc.next().toUpperCase();
        if (answer.equals("YES")) {
            System.out.println("cool, have fun i guess");
            for (int i = 0; i < people.size(); i++) {
                Person person = people.get(i);
                person.bustFlag = false;
            }
            return true;
        } else if (answer.equals("NO")) {
            System.out.println("goodbye then");
            return false;
        } else {
            System.out.println("i'll take that as a no, goodbye");
            return false;
        }
    }
*/
    public static void showSystemMessage(String message) {
        System.out.println("*** " + message + " ***");
    }
}

