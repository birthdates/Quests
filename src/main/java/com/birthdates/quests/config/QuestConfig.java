package com.birthdates.quests.config;

import com.birthdates.quests.quest.Quest;
import com.birthdates.quests.quest.QuestType;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Handles quest config logic
 */
public interface QuestConfig {
    void createQuest(String id, QuestType questType);

    CompletableFuture<Void> deleteQuest(String id);

    void saveQuest(Quest quest);

    void invalidateCache(String id);

    List<Quest> getAllQuests();

    Quest getQuest(String id);
}
