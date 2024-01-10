package com.birthdates.quests.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class QuestDataLoadedEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    public QuestDataLoadedEvent(@NotNull Player who) {
        super(who);
    }

    public static void callEvent(Player player) {
        Bukkit.getPluginManager().callEvent(new QuestDataLoadedEvent(player));
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
