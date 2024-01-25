package com.example.transferablefool;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class Player {
    private final List<Card> cardsInHand;

    public Player() {
        this.cardsInHand = new ArrayList<>();
    }

    public void receiveCard(final Card card, final Card trumpCard) {
        if (card != null) {
            this.cardsInHand.add(card);
            this.sortCardsInHand(trumpCard);
        }
    }

    public void receiveCards(final List<Card> cards, final Card trumpCard) {
        if (cards != null && cards.size() > 0) {
            this.cardsInHand.addAll(cards);
            this.sortCardsInHand(trumpCard);
        }
    }

    public void removeCardFromHand(final Card card) {
        this.cardsInHand.remove(card);
    }

    //сортировка карт в руках на основе их кодов
    private void sortCardsInHand(final Card trumpCard) {
        this.cardsInHand.sort(Comparator.comparing(card -> card.getCode(trumpCard)));
    }

    public List<Card> getCardsInHand() {
        return this.cardsInHand;
    }

    public boolean isHandEmpty() {
        return this.cardsInHand.isEmpty();
    }

    public int getNumberOfCardsInHand() {
        return this.cardsInHand.size();
    }
}