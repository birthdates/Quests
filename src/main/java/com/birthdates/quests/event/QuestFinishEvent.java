package com.birthdates.quests.event;

import com.birthdates.quests.quest.Quest;
import com.birthdates.quests.quest.QuestProgress;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class QuestFinishEvent extends QuestEvent {
    private static final HandlerList handlers = new HandlerList();

    public QuestFinishEvent(UUID player, Quest quest, QuestProgress questProgress) {
        super(player, quest, questProgress, false);
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public static void callEvent(UUID player, Quest quest, QuestProgress questProgress) {
        Bukkit.getPluginManager().callEvent(new QuestFinishEvent(player, quest, questProgress));
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
