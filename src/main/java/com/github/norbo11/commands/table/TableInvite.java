package com.github.norbo11.commands.table;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.github.norbo11.commands.PluginCommand;
import com.github.norbo11.game.cards.CardsPlayer;
import com.github.norbo11.util.ErrorMessages;
import com.github.norbo11.util.Messages;

public class TableInvite extends PluginCommand {
    public TableInvite() {
        getAlises().add("invite");
        getAlises().add("i");

        setDescription("Invites the specified player to your table.");

        setArgumentString("[player name]");

        getPermissionNodes().add(PERMISSIONS_BASE_NODE + "cards");
        getPermissionNodes().add(PERMISSIONS_BASE_NODE + "cards." + getAlises().get(0));
    }

    private String toInvite;

    private CardsPlayer cardsPlayer;

    @Override
    public boolean conditions() {
        if (getArgs().length == 2) {
            cardsPlayer = CardsPlayer.getCardsPlayer(getPlayer().getName());
            if (cardsPlayer != null) {
                Player playerToInvite = Bukkit.getPlayer(getArgs()[1]);
                if (playerToInvite != null) // If the player specified is an online player (ignoring the case), then send them the invite.
                {
                    toInvite = getArgs()[1];
                    return true;
                } else {
                    ErrorMessages.playerNotFound(getPlayer(), getArgs()[1]);
                }
            } else {
                ErrorMessages.notSittingAtTable(getPlayer());
            }
        } else {
            showUsage();
        }
        return false;
    }

    // Sends a simple message to the specified player to invite
    @Override
    public void perform() {
        Messages.sendMessage(toInvite, "&6" + getPlayer().getName() + " &fhas invited you to their poker table! Sit with &6/table sit " + cardsPlayer.getTable().getId() + " [buy-in]");
        Messages.sendMessage(getPlayer(), "You have invited &6" + toInvite + " &fto your table.");

    }
}
