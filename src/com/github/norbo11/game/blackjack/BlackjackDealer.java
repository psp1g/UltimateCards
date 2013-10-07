package com.github.norbo11.game.blackjack;

import java.util.Arrays;
import java.util.List;

import com.github.norbo11.game.cards.Card;
import com.github.norbo11.game.cards.Hand;
import com.github.norbo11.util.Formatter;
import com.github.norbo11.util.Messages;
import com.github.norbo11.util.MoneyMethods;
import com.github.norbo11.util.Sound;

public class BlackjackDealer {
    public BlackjackDealer(BlackjackTable table) {
        this.table = table;
    }

    private int score = 0;
    private Card holeCard = null;
    private BlackjackTable table;
    private Hand hand = new Hand();

    private boolean bust;

    public void addCards(Card[] cards) {
        for (Card card : cards) {
            getHand().getCards().add(card);
            Messages.sendToAllWithinRange(table.getLocation(), "&6" + "The dealer&f has been dealt the " + card.toString());
        }
    }

    public void addInitialCards() {
        List<Card> generated = Arrays.asList(table.getDeck().generateCards(2));
        getHand().getCards().addAll(generated);

        Messages.sendToAllWithinRange(table.getLocation(), "&6" + "The dealer&f has been dealt the " + generated.get(0).toString());
        holeCard = generated.get(1);
        recalculateScore();
        displayScore();
    }

    public void addMoney(double amount) {
        MoneyMethods.depositMoney(table.getOwner(), amount);
    }

    public void bust() {
        Messages.sendToAllWithinRange(getTable().getLocation(), "&6" + "The dealer&f has gone bust!");
        setBust(true);
    }

    public void checkForBust() {
        if (score > 21) {
            bust();
        }
    }

    public void displayScore() {
        Messages.sendToAllWithinRange(getTable().getLocation(), "&6" + "The dealer&f's score: &6" + score);
    }

    public Hand getHand() {
        return hand;
    }

    public Card getHoleCard() {
        return holeCard;
    }

    public double getMoney() {
        return MoneyMethods.getMoney(table.getOwner());
    }

    public int getScore() {
        return score;
    }

    public BlackjackTable getTable() {
        return table;
    }

    public boolean hasEnoughMoney(double amountToBet) {
        return amountToBet <= getMoney() / ((table.getPlayers().size() - 1) * 2);
    }

    public void hit() {
        addCards(table.getDeck().generateCards(1));
        recalculateScore();
        displayScore();
        checkForBust();
    }

    public boolean isBust() {
        return bust;
    }

    public boolean isUnderStayValue() {
        return score < 17;
    }

    public void pay(BlackjackPlayer blackjackPlayer, BlackjackHand hand) {
        if (blackjackPlayer.isPushing()) {
            blackjackPlayer.setPushing(0);
        }
        Messages.sendToAllWithinRange(table.getLocation(), "&6" + "The dealer (" + score + ")&f wins &6" + Formatter.formatMoney(hand.getAmountBet()) + "&f from &6" + blackjackPlayer.getPlayerName() + " (" + hand.getScore() + ")");
        Sound.lost(blackjackPlayer.getPlayer());
    }

    public void recalculateScore() {
        int newScore = 0;

        for (Card card : getHand().getCards())
            if (card != holeCard) {
                int cardScore = card.getBlackjackScore();
                newScore += cardScore;
                if (cardScore == 1) {
                    newScore += 10;
                    if (newScore > 21) {
                        newScore -= 10;
                    }
                }
            }

        score = newScore;
    }

    public void removeMoney(double amount) {
        MoneyMethods.withdrawMoney(table.getOwner(), amount);
    }

    public void reveal() {
        Messages.sendToAllWithinRange(table.getLocation(), "&6The dealer&f reveals a " + getHoleCard().toString());
        setHoleCard(null);
        recalculateScore();
        displayScore();
    }

    public void setBust(boolean bust) {
        this.bust = bust;
    }

    public void setHoleCard(Card holeCard) {
        this.holeCard = holeCard;
    }

    public void setScore(int score) {
        this.score = score;
    }

}
