package com.birthdates.quests.event;

import com.birthdates.quests.quest.Quest;
import com.birthdates.quests.quest.QuestProgress;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Called when a player progresses within a quest (useful to change amount)
 */
public class QuestProgressEvent extends QuestEvent {

    private static final HandlerList handlers = new HandlerList();
    private BigDecimal amount;

    public QuestProgressEvent(UUID playerId, Quest quest, QuestProgress questProgress, BigDecimal amount) {
        super(playerId, quest, questProgress);
        this.amount = amount;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public static BigDecimal callEvent(UUID player, Quest quest, QuestProgress questProgress, BigDecimal amount) {
        QuestProgressEvent event = new QuestProgressEvent(player, quest, questProgress, amount);
        Bukkit.getPluginManager().callEvent(event);
        return event.getAmount();
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
