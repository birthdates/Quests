package com.birthdates.quests.event;

import com.birthdates.quests.quest.Quest;
import com.birthdates.quests.quest.QuestProgress;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class QuestExpiredEvent extends QuestEvent {
    private static final HandlerList handlers = new HandlerList();

    public QuestExpiredEvent(UUID player, Quest quest, QuestProgress questProgress) {
        super(player, quest, questProgress, true);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public static void callEvent(UUID player, Quest quest, QuestProgress questProgress) {
        Bukkit.getPluginManager().callEvent(new QuestExpiredEvent(player, quest, questProgress));
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
