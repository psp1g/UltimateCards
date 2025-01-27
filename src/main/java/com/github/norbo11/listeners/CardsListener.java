package com.github.norbo11.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;

import com.github.norbo11.game.cards.CardsPlayer;
import com.github.norbo11.util.MapMethods;
import com.github.norbo11.util.Messages;
import com.github.norbo11.util.config.PluginConfig;
import org.bukkit.map.MapView;

//This class prevents map duping by disallowing inventory clicks, item drops, and item pickups if the map isnt picked up by the player it was supposed to go to.
public class CardsListener implements Listener {
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        CardsPlayer cardsPlayer = CardsPlayer.getCardsPlayer(player.getName());

        if (cardsPlayer != null) {
            if (cardsPlayer.getTable().getSettings().autoKickOnLeave.getValue())
                cardsPlayer.getTable().kick(cardsPlayer);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        CardsPlayer cardsPlayer = CardsPlayer.getCardsPlayer(player.getName());
        String command = e.getMessage().split(" ")[0];
        String[] allowables = new String[]{
                "/table",
                "/cards",
                "/poker",
                "/bj",
                "/blackjack"
        };

        if (cardsPlayer != null) {

            boolean allowed = false;

            for (String allowable : allowables) {
                if (allowable.equalsIgnoreCase(command)) allowed = true;
            }

            if (!player.isOp() && PluginConfig.isDisableCommandsWhilePlaying() && !allowed) {
                e.setCancelled(true);
                Messages.sendMessage(player, "You may not use any commands while playing cards!");
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (PluginConfig.isPreventMovementOutsideChatRange()) {
            Player player = e.getPlayer();
            CardsPlayer cardsPlayer = CardsPlayer.getCardsPlayer(player.getName());

            if (cardsPlayer != null) {
                int chatRange = PluginConfig.getPublicChatRange();
                if (e.getTo().distance(cardsPlayer.getTable().getSettings().startLocation.getValue()) >= chatRange) {
                    e.setCancelled(true);
                    Messages.sendMessage(player, "You may not move further than &6" + chatRange + " &fblocks from the table!");
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        ItemStack currentItem = e.getCurrentItem();
        if (currentItem == null || currentItem.getType() != Material.MAP) {
            return;
        }
        ItemStack itemStack = MapMethods.getSavedMaps().get(e.getWhoClicked().getName());
        if (currentItem.equals(itemStack)) {
            Messages.sendMessage((Player) e.getWhoClicked(), "You may not move your cards interface map!");
            e.setCancelled(true);
            e.getInventory().remove(itemStack);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent e) {
        if (e.getItemDrop().getItemStack().getType() != Material.FILLED_MAP) return;

        ItemStack itemStack = MapMethods.getSavedMaps().get(e.getPlayer().getName());

        if (itemStack == null || !e.getItemDrop().getItemStack().equals(itemStack)) return;

        Messages.sendMessage(e.getPlayer(), "You may not drop your cards interface map!");
        e.setCancelled(true);
    }

    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player player) || e.getItem().getItemStack().getType() != Material.FILLED_MAP)
            return;

        MapMeta mapMeta = (MapMeta) e.getItem().getItemStack().getItemMeta();
        if (mapMeta == null) return;
        MapView mapView = mapMeta.getMapView();
        if (mapView == null) return;

        int mapId = mapMeta.getMapView().getId();
        if (!MapMethods.getCreatedMaps().contains(mapId)) return;

        // If the player trying to pick up the map is NOT the rightful owner, and the
        // map DOES have an owner
        String mapOwner = MapMethods.mapExists(e.getItem().getItemStack());

        if (!mapOwner.equals(player.getName()) && !mapOwner.isEmpty()) {
            e.setCancelled(true);
        } else if (CardsPlayer.getCardsPlayer(player.getName()) == null) {
            e.setCancelled(true);
            e.getItem().remove();
            MapMethods.getCreatedMaps().remove((Integer) mapId);
        } // This results in maps that are no longer in use being deleted. Maps being
        // picked up, but still in use, are
        // not deleted, but simply cancelled. (both of this is in the case if the player
        // is not the owner of the map)
    }
}
