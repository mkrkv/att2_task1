package com.example.transferablefool;

public class FoolGame {
    private final Table table;
    private final HumanPlayer humanPlayer;
    private final BotPlayer botPlayer;
    private int numberOfCons;
    private boolean isPlayerTurn, isGameOver, isPlayerWon, isPlayerPassed, isBotPassed;


    public FoolGame() {
        this.table = new Table();
        this.humanPlayer = new HumanPlayer();
        this.botPlayer = new BotPlayer();
        this.numberOfCons = 0;

        this.isPlayerTurn = true;
        this.isGameOver = false;
        this.isPlayerWon = false;
        this.isPlayerPassed = false;
        this.isBotPassed = false;
    }

    public void start() {
        this.shuffleAndDealCards(); //перемешать колоду, выбрать козырь и раздать карты
        this.updateCardsPosition(); //обновить позиции карт на столе
    }

    public void shuffleAndDealCards() {
        this.table.shuffle();
        this.table.setTrumpCard();
        this.table.dealCards(this.humanPlayer, this.botPlayer, 6);
    }

    public void updateCardsPosition() {
        //карты бота - лицом книзу
        for (final Card botCard : this.botPlayer.getCardsInHand()) {
            if (botCard.isFaceUp()) {
                botCard.setFaceUp(false);
            }
        }

        //карты игрока - лицом кверху
        for (final Card playerCard : this.humanPlayer.getCardsInHand()) {
            if (!playerCard.isFaceUp()) {
                playerCard.setFaceUp(true);
            }
        }

        //карты на столе - лицом вкерху
        for (final Card cardOnTable : this.table.getCardsOnTable()) {
            if (!cardOnTable.isFaceUp()) {
                cardOnTable.setFaceUp(true);
            }
        }

        //обновление позиций карт бота
        final double botCardsStartX = 150 + (975 - ((this.botPlayer.getCardsInHand().size() - 1) * 25 + 100)) / 2;
        double botCardX = botCardsStartX;
        for (final Card botCard : this.botPlayer.getCardsInHand()) {
            botCard.relocate(botCardX, 25);
            botCardX += 25;
        }

        //обновление позиций карт игрока
        final double playerCardsStartX = 150 + (975 - ((this.humanPlayer.getCardsInHand().size() - 1) * 25 + 100)) / 2;
        double playerCardX = playerCardsStartX;
        for (final Card playerCard : this.humanPlayer.getCardsInHand()) {
            playerCard.relocate(playerCardX, 500);
            playerCardX += 25;
        }

        //обновление позиций карт на столе
        final double cardsOnTableStartX = 200 + (875 - ((this.table.getNumberOfAttackingCardsOnTable() - 1) * 25 + this.table.getNumberOfAttackingCardsOnTable() * 125)) / 2;
        double cardsOnTableX = cardsOnTableStartX;
        for (final Pair pairOnTable : this.table.getPairsOnTable()) {
            pairOnTable.getAttackingCard().relocate(cardsOnTableX, 250);
            if (pairOnTable.getDefendingCard() != null) {
                pairOnTable.getDefendingCard().relocate(cardsOnTableX + 25, 275);
            }
            cardsOnTableX += 150;
        }
    }

    public void checkGameOver() {
        this.isGameOver = this.table.isDeckEmpty() && (this.humanPlayer.isHandEmpty() || this.botPlayer.isHandEmpty());
        if (this.isGameOver) {
            this.isPlayerWon = this.humanPlayer.isHandEmpty();
        }
    }

    //смена сторон (атакующий/защищающийся) игрока и бота
    public void nextTurn() {
        this.isPlayerTurn = !this.isPlayerTurn;
    }

    public void increaseConsNumber() {
        this.numberOfCons++;
    }

    public HumanPlayer getPlayer() {
        return this.humanPlayer;
    }

    public BotPlayer getBot() {
        return this.botPlayer;
    }

    public int getConNumber() {
        return this.numberOfCons + 1;
    }

    public Table getTable() {
        return this.table;
    }

    public boolean isGameOver() {
        return this.isGameOver;
    }

    public boolean isPlayerWon() {
        return this.isPlayerWon;
    }

    public boolean isPlayerTurn() {
        return this.isPlayerTurn;
    }

    public boolean isPlayerPassed() {
        return this.isPlayerPassed;
    }

    public boolean isBotPassed() {
        return this.isBotPassed;
    }

    public void setPlayerPassed(final boolean playerPassed) {
        this.isPlayerPassed = playerPassed;
    }

    public void setBotPassed(final boolean botPassed) {
        this.isBotPassed = botPassed;
    }
}