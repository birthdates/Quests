package com.birthdates.quests.input;

import com.birthdates.quests.QuestPlugin;
import com.birthdates.quests.lang.LanguageService;
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

public class InputService implements Listener {
    private static final Map<UUID, Consumer<String>> AWAITING_INPUT = new ConcurrentHashMap<>();
    private static final Map<UUID, Menu> IN_MENU = new ConcurrentHashMap<>();

    public static CompletableFuture<String> awaitInput(Player player, String message) {
        CompletableFuture<String> future = new CompletableFuture<>();
        if (message != null) {
            String lang = QuestPlugin.getInstance().getLanguageService().get(message, player);
            player.sendMessage(LanguageService.color(lang));
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
        Bukkit.getScheduler().runTask(QuestPlugin.getInstance(), () ->
                QuestPlugin.getInstance().getMenuService().openMenu(event.getPlayer(), menu));
    }

}
