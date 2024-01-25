package com.example.transferablefool;

//пара карт: атакующая - защищающая
public class Pair {
    private Card attackingCard, defendingCard;

    public Pair(final Card attackingCard) {
        this.attackingCard = attackingCard;
        this.defendingCard = null;
    }

    //завершена ли, т. е. побита ли атакующая карта
    public boolean isComplete() {
        return this.defendingCard != null;
    }

    public Card getAttackingCard() {
        return this.attackingCard;
    }

    public Card getDefendingCard() {
        return this.defendingCard;
    }

    public void setAttackingCard(final Card attackingCard) {
        this.attackingCard = attackingCard;
    }

    public void setDefendingCard(final Card defendingCard) {
        this.defendingCard = defendingCard;
    }
}