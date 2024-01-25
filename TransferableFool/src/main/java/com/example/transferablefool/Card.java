package com.example.transferablefool;

import java.util.Arrays;
import java.util.List;

public class Card extends Rectangle {
    private final String suit, rank; //масть и ранг
    private boolean faceUp; //лежит ли лицом кверху

    public Card(final String suit, final String rank) {
        super(0, 0, 100, 150);
        this.suit = suit;
        this.rank = rank;
        this.faceUp = false;
    }

    public Card(final Card card) {
        super(card);
        this.suit = card.suit;
        this.rank = card.rank;
        this.faceUp = card.faceUp;
    }

    public boolean hasSameSuit(final Card card) {
        return this.suit.equals(card.suit);
    }

    public boolean hasSameRank(final Card card) {
        if (card != null) {
            return this.rank.equals(card.rank);
        } else {
            return false;
        }
    }

    public boolean hasBiggerRank(final Card card) {
        final List<String> ranks = List.of("6", "7", "8", "9", "10", "J", "Q", "K", "A");
        return ranks.indexOf(this.rank) > ranks.indexOf(card.rank);
    }

    public boolean canBeat(final Card card, final Card trumpCard) { //может ли текущая карта побить переданную
        return !this.suit.equals(card.suit) && this.suit.equals(trumpCard.suit) ||
                this.suit.equals(card.suit) && this.hasBiggerRank(card);
    }

    public int getCode(final Card trumpCard) { //код карты для сортировки в руках
        final String[] suits = {"♥", "♠", "♦", "♣"};
        final String[] ranks = {"6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        if (!this.hasSameSuit(trumpCard)) {
            return Arrays.stream(ranks).toList().indexOf(this.rank) * suits.length + Arrays.stream(suits).toList().indexOf(this.suit);
        } else {
            return Arrays.stream(ranks).toList().indexOf(this.rank) * suits.length + suits.length * ranks.length;
        }
    }

    public String getSuit() {
        return this.suit;
    }

    public String getRank() {
        return this.rank;
    }

    public boolean isFaceUp() {
        return this.faceUp;
    }

    public void setFaceUp(final boolean faceUp) {
        this.faceUp = faceUp;
    }
}