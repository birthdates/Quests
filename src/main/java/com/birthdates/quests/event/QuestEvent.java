package com.birthdates.quests.event;

import com.birthdates.quests.quest.Quest;
import com.birthdates.quests.quest.QuestProgress;
import org.bukkit.event.Event;

import java.util.UUID;

public abstract class QuestEvent extends Event {
    private final UUID playerId;
    private final Quest quest;
    private final QuestProgress questProgress;

    public QuestEvent(UUID playerId, Quest quest, QuestProgress questProgress) {
        this.playerId = playerId;
        this.quest = quest;
        this.questProgress = questProgress;
    }

    /**
     * Get the target player's ID (maybe offline)
     *
     * @return {@link UUID}
     */
    public UUID getPlayerId() {
        return playerId;
    }

    public Quest getQuest() {
        return quest;
    }

    public QuestProgress getQuestProgress() {
        return questProgress;
    }
}
