package Cards;

import java.util.Objects;

public class Card {
    static int nextId = 0;

    private int id;

    private CardSuit suit;
    private CardMeaning meaning;
    private int points;

    public Card(CardSuit suit, CardMeaning meaning, int points) {
        this.id = nextId++;

        this.suit = suit;
        this.meaning = meaning;
        this.points = points;
    }

    public CardMeaning getMeaning() {
        return meaning;
    }

    public CardSuit getSuit() {
        return suit;
    }

    public int getPoints() {
        return points;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;

        Card card = (Card) o;

        return card.hashCode() == hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, suit, meaning, points);
    }

    @Override
    public String toString() {
        return suit.toString() + " " + meaning.toString() + " (" + points + ")";
    }

}
