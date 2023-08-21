import CardGenerators.CardGenerator;
import CardGenerators.DefaultGenerator;
import Cards.Card;
import GameRules.DefaultRules;
import Inet.ClientThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {

    static Scanner sc = new Scanner(System.in);

    static ArrayList<Person> people;
    static Dealer dealer;

    static ArrayList<Card> deck;

    static CardGenerator cardGenerator = new DefaultGenerator();

    static Dictionary<String, Integer> bets;

    static int cardLimit = 22;

    public Server(int port) {
        al = new ArrayList<>();
        sdf = new SimpleDateFormat("HH:mm:ss");
        this.port = port;
    }

    public void game() {
        playerRegistration();
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
                dealerGetsCards();
                betClash();
                clearingHands();
            }
            playCondition = endingGame();

        } while (playCondition);

    }

    private final ArrayList<ClientThread> al;
    private final SimpleDateFormat sdf;
    private final int port;
    private boolean keepGoing;
    private int playercount;


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

            System.out.println("How many people are playing (1-4)");
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


    public void playerRegistration() {
        people = new ArrayList<>(playercount);
        dealer = new Dealer();

        System.out.println("What's dealer's name?");
        dealer.setName(sc.next());

        for (int j = 0; j < playercount; j++) {
            System.out.println("What is player " + (j + 1) + "'s name");
            Person person = new Person();
            person.setName(sc.next());

            people.add(person);
        }
    }

    public void deckCreation() {
        deck = cardGenerator.generateDeck();
    }

    public void makingBets() {
        bets = new Hashtable<>();
        for (int j = 0; j < people.size(); j++) {
            String name = people.get(j).getName();

            System.out.println("Player " + name + ", make your bet");
            int bet = sc.nextInt();

            bets.put(name, bet);
        }
    }

    public void dealingCards() {
        for (int i = 0; i < 2; i++) {
            dealer.addCard(getCardFromDeck());
        }
        for (int i = 0; i < people.size(); i++) {
            Person person = people.get(i);
            for (int j = 0; j < 2; j++) {
                person.addCard(getCardFromDeck());
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
        for (int i = 0; i < people.size(); i++) {
            System.out.println("These are cards of the player " + (people.get(i).getName()));
            var cards = people.get(i).getCards();
            for (int j = 0; j < cards.size(); j++) {
                System.out.println(cards.get(j));
            }
        }
        System.out.println("Card of the dealer is " + dealer.getCards().get(1));
    }

    public boolean checkBlackjack() {
        DefaultRules rules = new DefaultRules();
        int dealerSum = rules.cardCounter(dealer.getCards());
        boolean condition = false;

        if (dealerSum == 21) {

            for (int i = 0; i < people.size(); i++) {
                Person person = people.get(i);
                int playerSm = rules.cardCounter(person.getCards());
                int bet = bets.get(person.getName());

                if (playerSm != dealerSum) {
                    System.out.println("Player " + person.getName() + " doesn't have a blackjack! Its a lose!");
                    person.removeMoney(bet);
                    dealer.addMoney(bet);
                } else {
                    System.out.println("Player " + person.getName() + " has blackjack too! Its a push!");
                }
                bets.remove(person.getName());
                condition = true;
            }
        } else {
            for (int i = 0; i < people.size(); i++) {
                Person person = people.get(i);
                int playerSum = rules.cardCounter(person.getCards());
                int bet = bets.get(person.getName());

                if (playerSum == 21) {
                    System.out.println(person.getName() + " has a blackjack! (bonus 1500 cuz lazy to do 3/2)");
                    person.addMoney(bet + 1500);
                    dealer.removeMoney(bet + 1500);
                    bets.remove(person.getName());
                    person.bustFlag = true;
                }
            }
            condition = false;
        }
        return condition;
    }

    //universal method
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

    public void hitOrStand() {
        DefaultRules rules = new DefaultRules();
        for (int j = 0; j < people.size(); j++) {
            Person person = people.get(j);

            System.out.println("Player " + person.getName() + "'s turn!");
            System.out.println("Hit or stand?");
            for (int i = 0; i < cardLimit; i++) {
                String choice = sc.next(); //may be unnecessary if i use equals()

                if (choice.equalsIgnoreCase("HIT")) {
                    person.addCard(getCardFromDeck());
                    int sumOfCards = rules.cardCounter(person.getCards());
                    if (sumOfCards > 21) {
                        System.out.println(person.getName() + " busts!");
                        showingPlayerCards(person);
                        person.removeMoney(bets.get(person.getName()));
                        dealer.addMoney(bets.get(person.getName()));
                        bets.remove(person.getName());
                        person.bustFlag = true;
                        break;
                    } else {
                        showingPlayerCards(person);
                    }
                } else if (choice.equalsIgnoreCase("STAND")) {
                    System.out.println("Player stands!");
                    showingPlayerCards(person);
                    break;
                } else {
                    System.out.println("Choose something valid boi (hit/stand are not case sensitive)");
                    --i;
                }


            }

        }

    }

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

    public void betClash() {
        DefaultRules rules = new DefaultRules();
        var dealerCards = dealer.getCards();
        var dealerSum = rules.cardCounter(dealerCards);

        for (int i = 0; i < people.size(); i++) {
            Person person = people.get(i);
            var playerCards = person.getCards();
            var playerSum = rules.cardCounter(playerCards);
            var bet = bets.get(person.getName());
            if (dealerSum > 21) { // if dealer busts
                if (person.bustFlag) {

                } else if (playerSum > 21) { // if player busts at the same time
                    System.out.println("Dealer gets the bet of the player " + person.getName() + " because the player busted");
                    person.removeMoney(bet);
                    dealer.addMoney(bet);
                    bets.remove(person.getName());

                } else if (playerSum == 21) { // if player has blackjack
                    System.out.println("Player " + person.getName() + " wins the bet because dealer busted!");
                    person.addMoney(bet);
                    dealer.removeMoney(bet);
                    bets.remove(person.getName()); //unnecessary method

                } else { // if player exists and is an actual functioning member of society playing blackjack (member of society that plays blackjack sure)
                    System.out.println("Player " + person.getName() + " wins the bet because dealer busted! (bonus)");
                    person.addMoney(bet); // 3/2 but 2/1
                    dealer.removeMoney(bet);
                    bets.remove(person.getName());
                }

            } else {
                for (int j = 0; j < people.size(); j++) { // if dealer does not bust
                    if (person.bustFlag) {

                    } else if (playerSum < dealerSum) { //player's sum of cards is less than the dealer's
                        System.out.println("Player " + person.getName() + " busts, dealer takes the bet.");
                        person.removeMoney(bet);
                        dealer.addMoney(bet);
                        bets.remove(person.getName());
                    } else if (playerSum > dealerSum) { // player's winning
                        System.out.println("Player " + person.getName() + " wins the bet!");
                        person.addMoney(bet);
                        dealer.removeMoney(bet);
                        bets.remove(person.getName());
                    } else { // both push (dealer's sum == player's sum)
                        System.out.println("Both player " + person.getName() + " and dealer push (draw).");
                        bets.remove(person.getName());
                        }
                    }
                }
            }
        }

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

}

