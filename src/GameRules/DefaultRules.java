package GameRules;

import Cards.Card;
import Cards.CardMeaning;

import java.util.ArrayList;

public class DefaultRules {

    public int cardCounter(ArrayList<Card> cards) {
        int sumOfCards = 0;
        int amountOfAces = 0;
        for (int position = 0; position < cards.size(); position++) {
            sumOfCards += cards.get(position).getPoints();
            if (cards.get(position).getMeaning() == CardMeaning.Ace) {
                ++amountOfAces;
            }
        }
        for (int i = 0; i < amountOfAces; i++) {
            if (sumOfCards > 21) {
                sumOfCards -= 10;
            }
        }

        return sumOfCards;
    }
}
