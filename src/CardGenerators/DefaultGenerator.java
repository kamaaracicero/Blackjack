package CardGenerators;

import Cards.Card;
import Cards.CardMeaning;

import java.util.ArrayList;
import java.util.List;

public class DefaultGenerator implements CardGenerator {

    @Override
    public ArrayList<Card> generateDeck() {
        ArrayList<Card> cards = new ArrayList<>();
        cards.addAll(List.of(get4Cards(11, CardMeaning.Ace)));
        cards.addAll(List.of(get4Cards(2, CardMeaning.Two)));
        cards.addAll(List.of(get4Cards(3, CardMeaning.Three)));
        cards.addAll(List.of(get4Cards(4, CardMeaning.Four)));
        cards.addAll(List.of(get4Cards(5, CardMeaning.Five)));
        cards.addAll(List.of(get4Cards(6, CardMeaning.Six)));
        cards.addAll(List.of(get4Cards(7, CardMeaning.Seven)));
        cards.addAll(List.of(get4Cards(8, CardMeaning.Eight)));
        cards.addAll(List.of(get4Cards(9, CardMeaning.Nine)));
        cards.addAll(List.of(get4Cards(10, CardMeaning.Ten)));
        cards.addAll(List.of(get4Cards(10, CardMeaning.Jack)));
        cards.addAll(List.of(get4Cards(10, CardMeaning.Queen)));
        cards.addAll(List.of(get4Cards(10, CardMeaning.King)));

        return cards;
    }
}
