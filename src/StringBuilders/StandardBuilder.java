package StringBuilders;

import Cards.Card;

import java.util.ArrayList;

public class StandardBuilder implements CardsStringBuilder {

    @Override
    public String getCardsString(ArrayList<Card> cards, String startMessage) {
        StringBuilder str = new StringBuilder();
        str.append(startMessage);
        for (var card : cards) {
            str.append("   ");
            str.append(card.toString());
            str.append("\n");
        }

        return str.toString();
    }
}
