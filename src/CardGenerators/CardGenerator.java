package CardGenerators;

import Cards.Card;
import Cards.CardMeaning;
import Cards.CardSuit;

import java.util.ArrayList;

public interface CardGenerator {
    ArrayList<Card> generateDeck();

    default Card[] get4Cards(int points, CardMeaning meaning) {
        return new Card[] {
                new Card(CardSuit.Hearts, meaning, points),
                new Card(CardSuit.Clubs, meaning, points),
                new Card(CardSuit.Spades, meaning, points),
                new Card(CardSuit.Diamonds, meaning, points),
        };
    }
}
