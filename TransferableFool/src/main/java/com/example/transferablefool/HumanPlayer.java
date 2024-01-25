package com.example.transferablefool;

public class HumanPlayer extends Player {
    private Card selectedCardInHand, selectedCardOnTable; //карты, которые выбрал игрок (в руках | на столе)

    public HumanPlayer() {
        super();
        this.selectedCardInHand = null;
        this.selectedCardOnTable = null;
    }

    public Card showSelectedCardInHand() {
        return this.selectedCardInHand;
    }

    public Card showSelectedCardOnTable() {
        return this.selectedCardOnTable;
    }

    public void selectCardInHand(final Card selectedCardInHand) {
        this.selectedCardInHand = selectedCardInHand;
    }

    public void selectCardOnTable(final Card selectedCardOnTable) {
        this.selectedCardOnTable = selectedCardOnTable;
    }
}