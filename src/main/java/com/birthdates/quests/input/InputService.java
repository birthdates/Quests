package com.birthdates.quests.input;

import com.birthdates.quests.QuestPlugin;
import com.birthdates.quests.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Service to handle input logic from users
 */
public class InputService implements Listener {
    private static final Map<UUID, Consumer<String>> AWAITING_INPUT = new ConcurrentHashMap<>();
    private static final Map<UUID, Menu> IN_MENU = new ConcurrentHashMap<>();

    /**
     * Await chat input from a player
     *
     * @param player  Target player
     * @param message Language message to send player before awaiting input
     * @return Future that will be completed with the player's input
     */
    public static CompletableFuture<String> awaitInput(Player player, String message) {
        CompletableFuture<String> future = new CompletableFuture<>();
        if (message != null) {
            QuestPlugin.getInstance().getLanguageService().display(player, message);
        }

        Menu menu = QuestPlugin.getInstance().getMenuService().getMenu(player);
        if (menu != null) {
            QuestPlugin.getInstance().getMenuService().exemptClose(player.getUniqueId());
            player.closeInventory();
            IN_MENU.put(player.getUniqueId(), menu);
        }

        AWAITING_INPUT.put(player.getUniqueId(), future::complete);
        return future;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        AWAITING_INPUT.remove(event.getPlayer().getUniqueId());
        IN_MENU.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Consumer<String> consumer = AWAITING_INPUT.remove(uuid);
        if (consumer == null) {
            return;
        }

        event.setCancelled(true);
        consumer.accept(event.getMessage());
        Menu menu = IN_MENU.remove(uuid);
        if (menu == null) {
            return;
        }

        // Menus must be opened on the main thread
        Bukkit.getScheduler().runTask(QuestPlugin.getInstance(), () ->
                QuestPlugin.getInstance().getMenuService().openMenu(event.getPlayer(), menu));
    }

}
