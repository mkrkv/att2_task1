package com.example.transferablefool;

import javafx.animation.AnimationTimer;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class GameController {
    @FXML
    private Canvas canvas;
    @FXML
    private Label stateLabel, botMessageLabel;
    @FXML
    private Button insertCardButton, transferCardButton, beatCardButton, passButton;

    private GraphicsContext gc;
    private Rectangle cardsOnTableArea, cardsInPlayerHandArea;
    private FoolGame game;

    @FXML
    private void initialize() {
        this.cardsOnTableArea = new Rectangle(200, 250, 875, 175);
        this.cardsInPlayerHandArea = new Rectangle(150, 475, 975, 175);

        this.canvas.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                final double clickX = event.getX();
                final double clickY = event.getY();

                //проверка, по какой атакующей карте на столе был клик
                if (this.cardsOnTableArea.isPointInside(clickX, clickY)) {
                    boolean shouldCheckOtherCards = true;

                    //если клик по уже выбранной карте - снятие выделения карты
                    if (this.game.getPlayer().showSelectedCardOnTable() != null) {
                        final Card movedCardOnTable = new Card(this.game.getPlayer().showSelectedCardOnTable());
                        movedCardOnTable.move(0, -25);
                        if (movedCardOnTable.isPointInside(clickX, clickY)) {
                            this.game.getPlayer().selectCardOnTable(null);
                            shouldCheckOtherCards = false;
                        }
                    }

                    //если клик был не по уже выбранной карте
                    if (shouldCheckOtherCards) {
                        //установка выбранной карты
                        for (final Pair pairOnTable : this.game.getTable().getPairsOnTable()) {
                            if (!pairOnTable.isComplete() && pairOnTable.getAttackingCard().isPointInside(clickX, clickY)) {
                                this.game.getPlayer().selectCardOnTable(pairOnTable.getAttackingCard());
                                break;
                            }
                        }
                    }

                    //проверка, по какой карте в руках игрока был клик
                } else if (this.cardsInPlayerHandArea.isPointInside(clickX, clickY)) {
                    boolean shouldCheckOtherCards = true;

                    //если клик по уже выбранной карте - снятие выделения карты
                    if (this.game.getPlayer().showSelectedCardInHand() != null) {
                        final Card movedCardInHand = new Card(this.game.getPlayer().showSelectedCardInHand());
                        movedCardInHand.move(0, -25);
                        if (movedCardInHand.isPointInside(clickX, clickY)) {
                            this.game.getPlayer().selectCardInHand(null);
                            shouldCheckOtherCards = false;
                        }
                    }

                    //если клик был не по уже выбранной карте
                    if (shouldCheckOtherCards) {
                        //установка выбранной карты
                        for (final Card cardInHand : this.game.getPlayer().getCardsInHand()) {
                            if (cardInHand != this.game.getPlayer().showSelectedCardInHand() && cardInHand.isPointInside(clickX, clickY)) {
                                this.game.getPlayer().selectCardInHand(cardInHand);
                            }
                        }
                    }
                }
            }
        });

        this.gc = this.canvas.getGraphicsContext2D();

        this.stateLabel.setText("");
        this.botMessageLabel.setText("");

        //таймер,  работает с периодичность 1 / (частота обновления монитора)
        //т. е. за секунду выдаёт число обновлений, равное частоте обновления монитора
        final AnimationTimer graphicsLoop = new AnimationTimer() {
            @Override
            public void handle(final long now) {
                drawGameScene(); //обновление игровой сцены
                updateLabels(); //обновление текста "Вы атакуете" / "Вы защищаетесь"
            }
        };
        graphicsLoop.start();

        this.game = new FoolGame();
        this.game.start();
    }

    //подкинуть (положить) карту
    @FXML
    private void onActionInsertCard() {
        if (!this.game.isGameOver() && this.game.isPlayerTurn()) {
            this.game.setPlayerPassed(false);

            final Table table = this.game.getTable();
            final HumanPlayer player = this.game.getPlayer();
            final BotPlayer bot = this.game.getBot();
            final Card selectedCardInHand = player.showSelectedCardInHand();

            //снятие выделения с выбранных игроком карт
            if (player.showSelectedCardInHand() != null) {
                player.selectCardInHand(null);
            }
            if (player.showSelectedCardOnTable() != null) {
                player.selectCardOnTable(null);
            }

            if (selectedCardInHand != null && (table.isTableEmpty() ||
                    table.getNumberOfAttackingCardsOnTable() < 6 && table.getNumberOfUndefeatedAttackingCardsOnTable() + 1 <= bot.getNumberOfCardsInHand() && table.isThereSameCardOnTable(selectedCardInHand))) {

                //кладёт атакующую карту на стол
                table.putAttackingCardOnTable(player, selectedCardInHand);
                this.game.updateCardsPosition();
                this.game.checkGameOver();

                if (!this.game.isGameOver()) {
                    //очередь ходить бота

                    turnOnButtons(this, false); //отключение кнопок на время "раздумий" бота
                    //таймер, который ожидает заданное время и потом выполняет заданные действия
                    final PauseTransition pause = new PauseTransition(Duration.seconds(1)); //бот думает 1 секунду
                    //когда бот "подумает"
                    pause.setOnFinished(e -> {
                        turnOnButtons(this, true); //включение кнопок
                        bot.makeMove(this, this.game); //ход бота
                        //если игра завершена
                        if (this.game.isGameOver()) {
                            //Platform.runLater - для того, чтобы не завис UI
                            Platform.runLater(() -> showModalWindowGameOver(this.game.isPlayerWon())); //вывод сообщения о победе
                        }
                    });
                    pause.play(); //запуск таймера

                    //если игра завершена
                } else {
                    showModalWindowGameOver(this.game.isPlayerWon()); //вывод сообщения о победе
                }
            }
        }
    }

    //перевести карту
    @FXML
    private void onActionTransferCard() {
        if (!this.game.isGameOver() && !this.game.isPlayerTurn()) {
            this.game.setPlayerPassed(false);

            final Table table = this.game.getTable();
            final HumanPlayer player = this.game.getPlayer();
            final BotPlayer bot = this.game.getBot();
            final Card selectedCardInHand = player.showSelectedCardInHand();

            //снятие выделения с выбранных игроком карт
            if (player.showSelectedCardInHand() != null) {
                player.selectCardInHand(null);
            }
            if (player.showSelectedCardOnTable() != null) {
                player.selectCardOnTable(null);
            }

            if (this.game.getConNumber() > 1 && !table.isTableEmpty() && !table.isThereDefendingCardsOnTable() && selectedCardInHand != null &&
                    table.getNumberOfAttackingCardsOnTable() < 6 && table.getNumberOfUndefeatedAttackingCardsOnTable() + 1 <= bot.getNumberOfCardsInHand() && table.isThereSameCardOnTable(selectedCardInHand)) {

                //кладёт атакующую карту на стол
                table.putAttackingCardOnTable(player, selectedCardInHand);
                this.game.updateCardsPosition();
                this.game.checkGameOver();

                if (!this.game.isGameOver()) {
                    this.game.nextTurn(); //перевод очереди атаки (если атаковал игрок - теперь атакует бот) - перевод картой

                    //очередь ходить бота
                    turnOnButtons(this, false);
                    final PauseTransition pause = new PauseTransition(Duration.seconds(1));
                    pause.setOnFinished(e -> {
                        turnOnButtons(this, true);
                        bot.makeMove(this, this.game);
                        if (this.game.isGameOver()) {
                            Platform.runLater(() -> showModalWindowGameOver(this.game.isPlayerWon()));
                        }
                    });
                    pause.play();

                } else {
                    showModalWindowGameOver(this.game.isPlayerWon());
                }
            }
        }
    }

    //побить карту
    @FXML
    private void onActionBeatCard() {
        if (!this.game.isGameOver() && !this.game.isPlayerTurn()) {
            this.game.setPlayerPassed(false);

            final Table table = this.game.getTable();
            final HumanPlayer player = this.game.getPlayer();
            final BotPlayer bot = this.game.getBot();
            final Card selectedCardInHand = player.showSelectedCardInHand();
            final Card selectedCardOnTable = player.showSelectedCardOnTable();

            if (!table.isTableEmpty() && selectedCardInHand != null && selectedCardOnTable != null &&
                    selectedCardInHand.canBeat(selectedCardOnTable, table.getTrumpCard())) {

                //кладёт защищающую карту на стол - бьётся картой
                table.putDefendingCardOnTable(player, selectedCardOnTable, selectedCardInHand);
                this.game.updateCardsPosition();
                this.game.checkGameOver();

                if (!this.game.isGameOver()) {
                    //очередь ходить бота
                    turnOnButtons(this, false);
                    final PauseTransition pause = new PauseTransition(Duration.seconds(1));
                    pause.setOnFinished(e -> {
                        turnOnButtons(this, true);
                        bot.makeMove(this, this.game);
                        if (this.game.isGameOver()) {
                            Platform.runLater(() -> showModalWindowGameOver(this.game.isPlayerWon()));
                        }
                    });
                    pause.play();

                } else {
                    showModalWindowGameOver(this.game.isPlayerWon());
                }
            }
        }
    }

    //пас
    @FXML
    private void onActionPass() {
        final Table table = this.game.getTable();
        if (!this.game.isGameOver() && !table.isTableEmpty()) {
            this.game.setPlayerPassed(true);

            final HumanPlayer player = this.game.getPlayer();
            final BotPlayer bot = this.game.getBot();

            //снятие выделения с выбранных игроком карт
            if (player.showSelectedCardInHand() != null) {
                player.selectCardInHand(null);
            }
            if (player.showSelectedCardOnTable() != null) {
                player.selectCardOnTable(null);
            }

            // если атакует бот
            if (!this.game.isPlayerTurn()) {
                this.game.setPlayerPassed(true);

                //очередь ходить бота
                turnOnButtons(this, false);
                final PauseTransition pause = new PauseTransition(Duration.seconds(1));
                pause.setOnFinished(e -> {
                    turnOnButtons(this, true);
                    bot.makeMove(this, this.game);
                    if (this.game.isGameOver()) {
                        Platform.runLater(() -> showModalWindowGameOver(this.game.isPlayerWon()));
                    }
                });
                pause.play();

            } else {
                //если до этого пасанул бот
                if (this.game.isBotPassed()) {
                    table.receiveCardsFromTable(bot); //бот собирает карты со стола

                    this.game.setPlayerPassed(false);
                    this.game.setBotPassed(false);

                    this.game.getTable().drawCardsUpToLimit(player, bot, this.game.isPlayerTurn());
                    this.game.updateCardsPosition();
                    this.game.checkGameOver();

                    if (!this.game.isGameOver()) {
                        this.game.increaseConsNumber();
                    } else {
                        showModalWindowGameOver(this.game.isPlayerWon());
                    }

                    //если бот не пасанул
                } else {
                    //если остались карты на столе - ход бота, если нет - следующий кон

                    if (table.isAllAttackingCardsDefeated()) {
                        table.clearTable();

                        this.game.setPlayerPassed(false);
                        this.game.setBotPassed(false);

                        this.game.getTable().drawCardsUpToLimit(player, bot, this.game.isPlayerTurn());
                        this.game.updateCardsPosition();
                        this.game.checkGameOver();

                        if (!this.game.isGameOver()) {
                            this.game.increaseConsNumber();
                            this.game.nextTurn();

                            //очередь ходить бота
                            turnOnButtons(this, false);
                            final PauseTransition pause = new PauseTransition(Duration.seconds(1));
                            pause.setOnFinished(e -> {
                                turnOnButtons(this, true);
                                bot.makeMove(this, this.game);
                                if (this.game.isGameOver()) {
                                    Platform.runLater(() -> showModalWindowGameOver(this.game.isPlayerWon()));
                                }
                            });
                            pause.play();

                        } else {
                            showModalWindowGameOver(this.game.isPlayerWon());
                        }

                    } else {
                        turnOnButtons(this, false);
                        final PauseTransition pause = new PauseTransition(Duration.seconds(1));
                        pause.setOnFinished(e -> {
                            turnOnButtons(this, true);
                            bot.makeMove(this, this.game);
                            if (this.game.isGameOver()) {
                                Platform.runLater(() -> showModalWindowGameOver(this.game.isPlayerWon()));
                            }
                        });
                        pause.play();
                    }
                }
            }
        }
    }

    private void drawGameScene() {
        final Table table = this.game.getTable();

        //рисуется стол
        this.fillRect(0, 0, 1250, 675, Color.GREEN);

        //рисуется козырная карта под колодой
        this.drawCardSprite(table.getTrumpCard());

        //если колода пустая, поверх рисуется полупрозрачный стол, чтобы козырная карта казалась полупрозрачной
        if (table.isDeckEmpty()) {
            final Color color = new Color(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue(), 0.5);
            this.fillRect(0, 0, 1250, 675, color);
        }

        //рисуется колода над козырем, если в колоде больше 1 карты
        if (table.getDeckSize() > 1) {
            final Card randomCard = new Card("♥", "6"); // просто любая карта, имитирует колоду
            randomCard.relocate(25, 260);
            this.drawCardSprite(randomCard);
        }

        //рисуется бита, если хотя бы раз была бита
        if (table.hasDiscardPile()) {
            final Card randomCard = new Card("♥", "6"); // просто любая карта, имитирует колоду
            randomCard.relocate(1125, 260);
            this.drawCardSprite(randomCard);
        }

        //рисуются карты на столе (сначала подсветка выбранной карты на столе)
        for (int i = 0; i < 2; i++) {
            //в первом проходе рисуем атакующие карты,  во втором - защищающие
            for (final Pair pairOnTable : this.game.getTable().getPairsOnTable()) {
                //отрисовка каждой карты
                switch (i) {
                    case 0 -> {
                        if (pairOnTable.getAttackingCard() == this.game.getPlayer().showSelectedCardOnTable()) {
                            //если выбранная карта, рисуем карту с подсветкой и повыше
                            final Card selectedCardOnTable = new Card(pairOnTable.getAttackingCard());
                            selectedCardOnTable.move(0, -25);
                            this.fillRect(selectedCardOnTable.getX() - 4, selectedCardOnTable.getY() - 4, selectedCardOnTable.getWidth() + 8, selectedCardOnTable.getHeight() + 8, Color.RED);
                            this.drawCardSprite(selectedCardOnTable);

                        } else {
                            //отрисовка атакующей карты
                            this.drawCardSprite(pairOnTable.getAttackingCard());
                        }
                    }

                    case 1 -> {
                        //отрисовка защищающей карты
                        this.drawCardSprite(pairOnTable.getDefendingCard());
                    }
                }
            }
        }

        //рисуются карты бота
        for (final Card botCard : this.game.getBot().getCardsInHand()) {
            //отрисовка карты
            this.drawCardSprite(botCard);
        }


        //рисуются карты игрока
        for (final Card playerCard : this.game.getPlayer().getCardsInHand()) {
            if (playerCard != this.game.getPlayer().showSelectedCardInHand()) {
                this.drawCardSprite(playerCard);
            }
        }

        //поверх всех рисуется выбранная карта в руках
        if (this.game.getPlayer().showSelectedCardInHand() != null) {
            //если выбранная карта, рисуем карту с подсветкой и повыше
            final Card selectedCardInHand = new Card(this.game.getPlayer().showSelectedCardInHand());
            selectedCardInHand.move(0, -25);
            this.fillRect(selectedCardInHand.getX() - 4, selectedCardInHand.getY() - 4, selectedCardInHand.getWidth() + 8, selectedCardInHand.getHeight() + 8, Color.RED);
            this.drawCardSprite(selectedCardInHand);
        }
    }

    private void updateLabels() {
        if (this.game.isPlayerTurn()) {
            this.stateLabel.setText("Атакуйте!");
        } else {
            this.stateLabel.setText("Защищайтесь!");
        }

        //если бот пасует - вывод соответствующего сообщения
        if (this.game.isBotPassed()) {
            this.botMessageLabel.setText("\"Пасую!\"");
        } else {
            this.botMessageLabel.setText("");
        }
    }

    private void drawCardSprite(final Card card) {
        if (card != null) {
            final double x = card.getX(), y = card.getY(), width = card.getWidth(), height = card.getHeight();

            //отрисовка окаймовки
            this.fillRect(x, y, width, height, Color.BLACK);

            //если карта - лицом вверх, отрисовка лица карты
            if (card.isFaceUp()) {
                final String rank = card.getRank(), suit = card.getSuit();
                final Color color;

                switch (suit) {
                    case "♥", "♦" -> {
                        color = Color.RED;
                    }
                    case "♣", "♠" -> {
                        color = Color.BLACK;
                    }
                    default -> {
                        color = null;
                    }
                }

                //масть карты
                final Text rankText = new Text(rank);
                final Font rankFont = Font.font("Arial", FontWeight.BOLD, 25);
                rankText.setFont(rankFont);
                final double rankTextWidth = rankText.getLayoutBounds().getWidth(), rankTextHeight = rankText.getLayoutBounds().getHeight();

                //ранг карты
                final Text suitText = new Text(suit);
                final Font suitFont = Font.font("Arial", FontWeight.BOLD, 35);
                suitText.setFont(suitFont);
                final double suitTextWidth = suitText.getLayoutBounds().getWidth(), suitTextHeight = suitText.getLayoutBounds().getHeight();

                //большая масть карты (посередине карты)
                final Text bigSuitText = new Text(suit);
                final Font bigSuitFont = Font.font("Arial", FontWeight.BOLD, 75);
                bigSuitText.setFont(bigSuitFont);
                final double bigSuitTextWidth = bigSuitText.getLayoutBounds().getWidth(), bigSuitTextHeight = bigSuitText.getLayoutBounds().getHeight();

                //отрисовка белого фона карты (остаётся небольшая чёрная окаймовка)
                this.fillRect(x + 4, y + 4, width - 8, height - 8, Color.WHITE);

                //установка выбранного цвета (красного либо чёрного)
                this.gc.setFill(color);

                this.gc.setFont(rankFont);
                this.gc.fillText(rank, x + 6, y + rankTextHeight - 4); //ранг слева сверху
                this.gc.fillText(rank, x + width - rankTextWidth - 6, y + height - 6); //ранг справа снизу

                this.gc.setFont(suitFont);
                this.gc.fillText(suit, x + 6, y + suitTextHeight + rankTextHeight / 2 - 4); //масть слева сверху
                this.gc.fillText(suit, x + width - suitTextWidth - 6, y + height - rankTextHeight); //масть справа снизу

                this.gc.setFont(bigSuitFont);
                this.gc.fillText(suit, x + (width - bigSuitTextWidth) / 2, y + (height + bigSuitTextHeight / 2) / 2); //большая масть по центру

                //иначе - отрисовка рубашки карты
            } else {
                this.fillRect(x + 4, y + 4, width - 8, height - 8, Color.BROWN);
                this.fillRect(x + 12, y + 12, width - 24, height - 24, Color.BROWN);
            }
        }
    }

    private void fillRect(final double x, final double y, final double width, final double height, final Color color) {
        this.gc.setFill(color);
        this.gc.fillRect(x, y, width, height);
    }

    //включение / отключение кнопок
    public static void turnOnButtons(final GameController controller, final boolean isTurnOn) {
        if (isTurnOn) {
            controller.insertCardButton.setDisable(false);
            controller.transferCardButton.setDisable(false);
            controller.beatCardButton.setDisable(false);
            controller.passButton.setDisable(false);

        } else {
            controller.insertCardButton.setDisable(true);
            controller.transferCardButton.setDisable(true);
            controller.beatCardButton.setDisable(true);
            controller.passButton.setDisable(true);
        }
    }

    //модальное окно (небольшое окошко поверх основного) с сообщением о победе
    public static void showModalWindowGameOver(final boolean isPlayerWon) {
        final Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initStyle(StageStyle.UNDECORATED); //убираем кнопку-крестик (чтобы была только кнопка "Ок")
        alert.setTitle("Конец игры!");
        alert.setHeaderText(null);
        if (isPlayerWon) {
            alert.setContentText("Вы победили!");
        } else {
            alert.setContentText("Вы проиграли!");
        }

        //кнопка "Ок"
        final ButtonType okButton = new ButtonType("Ок");
        alert.getButtonTypes().setAll(okButton);

        //обработчик события для кнопки "Ок"
        alert.setOnCloseRequest(event -> {
            if (alert.getResult() == okButton) {
                //завершение работы приложения
                System.exit(0);
            }
        });

        //показ модального окна
        alert.showAndWait();
    }
}