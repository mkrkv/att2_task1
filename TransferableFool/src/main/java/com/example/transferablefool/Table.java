package com.example.transferablefool;

import java.util.*;

public class Table {
    private final Stack<Card> deck; //колода
    private Card trumpCard; //козырная карта
    private final List<Pair> cardsOnTable;
    private boolean hasDiscardPile; //была ли хотя бы один раз бита

    public Table() {
        this.deck = new Stack<>();
        final String[] suits = {"♥", "♠", "♦", "♣"};
        final String[] ranks = {"6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        for (final String suit : suits) {
            for (final String rank : ranks) {
                this.deck.push(new Card(suit, rank));
            }
        }
        this.trumpCard = null;
        this.cardsOnTable = new LinkedList<>();
        this.hasDiscardPile = false;
    }

    public void shuffle() {
        Collections.shuffle(this.deck);
    }

    public void setTrumpCard() {
        if (!this.deck.isEmpty()) {
            this.trumpCard = new Card(this.deck.firstElement());
            this.trumpCard.relocate(50, 260);
            this.trumpCard.setFaceUp(true);
        }
    }

    //добавить атакующую карту на стол (в виде новой незавершённой пары)
    public void putAttackingCardOnTable(final Player player, final Card attackingCard) {
        this.cardsOnTable.add(new Pair(attackingCard));
        player.removeCardFromHand(attackingCard);
        if (player instanceof HumanPlayer) {
            ((HumanPlayer) player).selectCardInHand(null);
        }
    }

    //добавить защищающуюся карту на стол (на атакующую карту; пара завершена)
    public void putDefendingCardOnTable(final Player player, final Card attackingCard, final Card defendingCard) {
        for (final Pair pair : this.cardsOnTable) {
            if (pair.getAttackingCard().equals(attackingCard)) {
                pair.setDefendingCard(defendingCard);
                break;
            }
        }

        player.removeCardFromHand(defendingCard);
        if (player instanceof HumanPlayer) {
            ((HumanPlayer) player).selectCardInHand(null);
            ((HumanPlayer) player).selectCardOnTable(null);
        }
    }

    //забрать карты со стола
    public void receiveCardsFromTable(final Player player) {
        player.receiveCards(this.getCardsOnTable(), this.trumpCard);
        this.cardsOnTable.clear();
    }

    public void clearTable() {
        this.cardsOnTable.clear();
        if (!this.hasDiscardPile) {
            this.hasDiscardPile = true;
        }
    }

    //раздача карт игрокам в начале игры
    public void dealCards(final HumanPlayer humanPlayer, final BotPlayer botPlayer, final int cardsPerPlayer) {
        for (int i = 0; i < cardsPerPlayer; i++) {
            if (!this.deck.isEmpty()) {
                humanPlayer.receiveCard(this.drawCard(), this.trumpCard);
                botPlayer.receiveCard(this.drawCard(), this.trumpCard);
            } else {
                break;
            }
        }
    }

    //набор карт после завершения кона
    public void drawCardsUpToLimit(final HumanPlayer player, final BotPlayer bot, final boolean isPlayerTurn) {
        final Player first = isPlayerTurn ? player : bot;
        final Player second = isPlayerTurn ? bot : player;

        while (first.getNumberOfCardsInHand() < 6 || second.getNumberOfCardsInHand() < 6) {
            if (first.getNumberOfCardsInHand() <= second.getNumberOfCardsInHand()) {
                if (first.getNumberOfCardsInHand() < 6) {
                    first.receiveCard(this.drawCard(), this.trumpCard);
                }
            }

            if (second.getNumberOfCardsInHand() <= first.getNumberOfCardsInHand()) {
                if (second.getNumberOfCardsInHand() < 6) {
                    second.receiveCard(this.drawCard(), this.trumpCard);
                }
            }

            if (this.isDeckEmpty()) {
                break;
            }
        }
    }

    private Card drawCard() {
        if (!this.deck.isEmpty()) {
            return this.deck.pop();
        } else {
            return null;
        }
    }

    public Card getTrumpCard() {
        return this.trumpCard;
    }

    public int getDeckSize() {
        return this.deck.size();
    }

    public boolean isDeckEmpty() {
        return this.deck.isEmpty();
    }

    public boolean isTableEmpty() {
        return this.cardsOnTable.isEmpty();
    }

    public boolean hasDiscardPile() {
        return this.hasDiscardPile;
    }

    public List<Card> getCardsOnTable() {
        final List<Card> cardsFromTable = new LinkedList<>();
        for (final Pair pair : this.cardsOnTable) {
            cardsFromTable.add(pair.getAttackingCard());
            if (pair.isComplete()) {
                cardsFromTable.add(pair.getDefendingCard());
            }
        }

        return cardsFromTable;
    }

    //получить не побитые атакующие карты на столе
    public List<Card> getUndefeatedAttackingCardsOnTable()  {
        final List<Card> undefeatedCardsFromTable = new LinkedList<>();
        for (final Pair pair : this.cardsOnTable) {
            if (!pair.isComplete()) {
                undefeatedCardsFromTable.add(pair.getAttackingCard());
            }
        }

        return undefeatedCardsFromTable;
    }

    public List<Pair> getPairsOnTable() {
        return this.cardsOnTable;
    }

    //получить число атакующих карт на столе
    public int getNumberOfAttackingCardsOnTable() {
        return this.cardsOnTable.size();
    }

    //получить число не побитых атакующих карт на столе
    public int getNumberOfUndefeatedAttackingCardsOnTable() {
        return this.getUndefeatedAttackingCardsOnTable().size();
    }

    //есть ли на столе карта с таким же рангом, необходимо при подкидывании и переводе
    public boolean isThereSameCardOnTable(final Card card) {
        for (final Pair pair : this.cardsOnTable) {
            if (pair.getAttackingCard().hasSameRank(card) || pair.isComplete() && pair.getDefendingCard().hasSameRank(card)) {
                return true;
            }
        }

        return false;
    }

    //есть ли на столе защищающиеся карты (т. е. начал ли защищающийся игрок биться; необходимо при переводе)
    public boolean isThereDefendingCardsOnTable() {
        for (final Pair pair : this.cardsOnTable) {
            if (pair.isComplete()) {
                return true;
            }
        }

        return false;
    }

    //побиты ли все атакующие карты
    public boolean isAllAttackingCardsDefeated() {
        for (final Pair pair : this.cardsOnTable) {
            if (!pair.isComplete()) {
                return false;
            }
        }

        return true;
    }
}