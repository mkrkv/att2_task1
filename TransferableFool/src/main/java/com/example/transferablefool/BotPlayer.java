package com.example.transferablefool;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;

import static com.example.transferablefool.GameController.showModalWindowGameOver;
import static com.example.transferablefool.GameController.turnOnButtons;

public class BotPlayer extends Player {

    public BotPlayer() {
        super();
    }

    public void makeMove(final GameController controller, final FoolGame game) {
        boolean isMoveMade = false;
        //если ходит игрок, приоритеты: перевести, побить, взять
        final Table table = game.getTable();
        final HumanPlayer player = game.getPlayer();

        if (game.isPlayerTurn()) {
            //пробует перевести
            if (game.getConNumber() > 1 && !table.isTableEmpty() && !table.isThereDefendingCardsOnTable() &&
                    table.getNumberOfAttackingCardsOnTable() < 6 && table.getNumberOfUndefeatedAttackingCardsOnTable() + 1 <= player.getNumberOfCardsInHand()) {
                game.setBotPassed(false);

                final Card selectedCardInHand = selectCardForTransfer(table);
                if (selectedCardInHand != null) {
                    isMoveMade = true;

                    table.putAttackingCardOnTable(this, selectedCardInHand);
                    game.updateCardsPosition();
                    game.checkGameOver();
                    if (!game.isGameOver()) {
                        game.nextTurn();
                    }
                }
            }

            //пробует побить
            if (!isMoveMade && !table.isTableEmpty()) {
                game.setBotPassed(false);

                final Card selectedCardInHand = selectCardForBeat(table);
                final Card selectedCardOnTable = selectCardForBeBeaten(table, selectedCardInHand);
                if (selectedCardInHand != null && selectedCardOnTable != null) {
                    isMoveMade = true;

                    table.putDefendingCardOnTable(this, selectedCardOnTable, selectedCardInHand);
                    game.updateCardsPosition();
                    game.checkGameOver();
                }
            }

            //пас
            if (!isMoveMade && !table.isTableEmpty()) {
                game.setBotPassed(true);
            }

            //если ходит бот, приоритеты: подкинуть, пас
        } else {
            //первый ход за кон или подкидывание
            if (table.isTableEmpty() || table.getNumberOfAttackingCardsOnTable() < 6 && table.getNumberOfUndefeatedAttackingCardsOnTable() + 1 <= player.getNumberOfCardsInHand()) {
                game.setBotPassed(false);

                final Card selectedCardInHand = selectCardForInsert(table);
                if (selectedCardInHand != null) {
                    isMoveMade = true;

                    table.putAttackingCardOnTable(this, selectedCardInHand);
                    game.updateCardsPosition();
                    game.checkGameOver();
                }
            }

            //пас
            if (!isMoveMade && !table.isTableEmpty()) {
                game.setBotPassed(true);

                //если до этого пасанул игрок - он забирает карты со стола и ходит бот
                if (game.isPlayerPassed()) {
                    table.receiveCardsFromTable(player);

                    game.setPlayerPassed(false);
                    game.setBotPassed(false);

                    game.getTable().drawCardsUpToLimit(player, this, game.isPlayerTurn());
                    game.updateCardsPosition();
                    game.checkGameOver();
                    if (!game.isGameOver()) {
                        game.increaseConsNumber();

                        turnOnButtons(controller, false);
                        final PauseTransition pause = new PauseTransition(Duration.seconds(1));
                        pause.setOnFinished(e -> {
                            turnOnButtons(controller, true);
                            this.makeMove(controller, game);
                            if (game.isGameOver()) {
                                Platform.runLater(() -> showModalWindowGameOver(game.isPlayerWon()));
                            }
                        });
                        pause.play();
                    }

                } else {
                    //если остались карты на столе - ход бота, если нет - следующий кон
                    if (table.isAllAttackingCardsDefeated()) {
                        table.clearTable();

                        game.setPlayerPassed(false);
                        game.setBotPassed(false);

                        game.getTable().drawCardsUpToLimit(player, this, game.isPlayerTurn());
                        game.updateCardsPosition();
                        game.checkGameOver();
                        if (!game.isGameOver()) {
                            game.increaseConsNumber();
                            game.nextTurn();
                        }
                    }
                }
            }
        }
    }

    //выбирает карту для перевода
    private Card selectCardForTransfer(final Table table) {
        Card selectedCardInHand = null;

        // поиск карты для перевода среди не козырных карт в руках
        for (final Card cardInHand : this.getCardsInHand()) {
            for (final Card cardOnTable : table.getCardsOnTable()) {

                if (!cardInHand.hasSameSuit(table.getTrumpCard()) && cardInHand.hasSameRank(cardOnTable) &&
                        (selectedCardInHand == null || selectedCardInHand.hasBiggerRank(cardInHand))) {
                    selectedCardInHand = cardInHand;
                }
            }
        }

        // поиск карты для перевода среди козырных карт в руках
        if (selectedCardInHand == null) {

            for (final Card cardInHand : this.getCardsInHand()) {
                for (final Card cardOnTable : table.getCardsOnTable()) {

                    if (cardInHand.hasSameSuit(table.getTrumpCard()) && cardInHand.hasSameRank(cardOnTable) &&
                            (selectedCardInHand == null || selectedCardInHand.hasBiggerRank(cardInHand))) {
                        selectedCardInHand = cardInHand;
                    }
                }
            }
        }

        return selectedCardInHand;
    }

    //выбирает карту для битья среди карт в руках
    private Card selectCardForBeat(final Table table) {
        //если на столе есть хотя бы одна карта, которую не может побить бот, он даже не будет пытаться бить остальные карты
        //чтобы ему не подкинули ещё больше карт
        //признак разумности бота

        //поиск карты на столе, которую будет бить
        for (final Card cardOnTable : table.getUndefeatedAttackingCardsOnTable()) {
            boolean couldBeDefeated = false;

            for (final Card cardInHand : this.getCardsInHand()) {
                if (cardInHand.canBeat(cardOnTable, table.getTrumpCard())) {
                    couldBeDefeated = true;
                    break;
                }
            }

            if (!couldBeDefeated) {
                return null;
            }
        }

        Card selectedCardInHand = null;

        // поиск карты для битья среди не козырных карт в руках
        for (final Card cardInHand : this.getCardsInHand()) {
            for (final Card cardOnTable : table.getUndefeatedAttackingCardsOnTable()) {

                if (!cardInHand.hasSameSuit(table.getTrumpCard()) && cardInHand.canBeat(cardOnTable, table.getTrumpCard()) &&
                        (selectedCardInHand == null || selectedCardInHand.hasBiggerRank(cardInHand))) {
                    selectedCardInHand = cardInHand;
                }
            }
        }

        // поиск карты для битья среди козырных карт в руках
        if (selectedCardInHand == null) {

            for (final Card cardInHand : this.getCardsInHand()) {
                for (final Card cardOnTable : table.getUndefeatedAttackingCardsOnTable()) {

                    if (cardInHand.hasSameSuit(table.getTrumpCard()) && cardInHand.canBeat(cardOnTable, table.getTrumpCard()) &&
                            (selectedCardInHand == null || selectedCardInHand.hasBiggerRank(cardInHand))) {
                        selectedCardInHand = cardInHand;
                    }
                }
            }
        }

        return selectedCardInHand;
    }

    //выбирает карту, которую будет бить, среди карт на столе
    private Card selectCardForBeBeaten(final Table table, final Card selectedCardInHand) {
        if (selectedCardInHand != null) {
            // поиск карты, которую побьёт, среди карт на столе
            for (final Card undefeatedCardOnTable : table.getUndefeatedAttackingCardsOnTable()) {
                if (selectedCardInHand.canBeat(undefeatedCardOnTable, table.getTrumpCard())) {
                    return undefeatedCardOnTable;
                }
            }
        }
        return null;
    }

    //выбирает карту для подкидывания среди карт в руках
    private Card selectCardForInsert(final Table table) {
        Card selectedCardInHand = null;

        // если первый ход за кон
        if (table.isTableEmpty()) {
            // поиск карты для хода среди не козырных карт в руках
            for (final Card cardInHand : this.getCardsInHand()) {
                if (!cardInHand.hasSameSuit(table.getTrumpCard()) && (selectedCardInHand == null || selectedCardInHand.hasBiggerRank(cardInHand))) {
                    selectedCardInHand = cardInHand;
                }
            }

            if (selectedCardInHand == null) {
                // поиск карты для хода среди козырных карт в руках
                for (final Card cardInHand : this.getCardsInHand()) {
                    if (cardInHand.hasSameSuit(table.getTrumpCard()) && (selectedCardInHand == null || selectedCardInHand.hasBiggerRank(cardInHand))) {
                        selectedCardInHand = cardInHand;
                    }
                }
            }

            // если подкидывает
        } else {
            // поиск карты для подкидывания среди не козырных карт в руках
            for (final Card cardInHand : this.getCardsInHand()) {
                for (final Card cardOnTable : table.getCardsOnTable()) {

                    if (!cardInHand.hasSameSuit(table.getTrumpCard()) && cardInHand.hasSameRank(cardOnTable) &&
                            (selectedCardInHand == null || selectedCardInHand.hasBiggerRank(cardInHand))) {
                        selectedCardInHand = cardInHand;
                    }
                }
            }

            // поиск карты для подкидывания среди козырных карт в руках
            if (selectedCardInHand == null) {
                for (final Card cardInHand : this.getCardsInHand()) {
                    for (final Card cardOnTable : table.getCardsOnTable()) {

                        if (cardInHand.hasSameSuit(table.getTrumpCard()) && cardInHand.hasSameRank(cardOnTable) &&
                                (selectedCardInHand == null || selectedCardInHand.hasBiggerRank(cardInHand))) {
                            selectedCardInHand = cardInHand;
                        }
                    }
                }
            }
        }

        return selectedCardInHand;
    }
}