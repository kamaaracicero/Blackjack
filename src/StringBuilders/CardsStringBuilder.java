package StringBuilders;

import Cards.Card;

import java.util.ArrayList;

public interface CardsStringBuilder {
    String getCardsString(ArrayList<Card> cards, String startMessage);
}
