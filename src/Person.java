import Cards.Card;

import java.util.ArrayList;

public class Person {
    int money;
    String name;
    ArrayList<Card> cards;
    boolean bustFlag;

    public Person(){
        this(5000, "");
    }

    public Person(int money, String name){
        this.money = money;
        this.name = name;
        this.cards = new ArrayList<>();
    }


    public ArrayList<Card> getCards() {
        return cards;
    }

    public int getMoney() {
        return money;
    }

    public String getName() {
        return name;
    }

    public void addCard(Card card) {
        cards.add(card);
        // ѕоказать добаленную карту
    }

    // метод показа всех карт

    public void clearHand(){
        cards.clear();
    }

    public void removeMoney(int money){
        this.money -= money;
    }

    public void addMoney(int money){
        this.money += money;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString(){
        return name + " " + money;
    }

}
