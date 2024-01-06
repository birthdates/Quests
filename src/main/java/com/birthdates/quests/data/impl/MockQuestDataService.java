package com.birthdates.quests.data.impl;

import com.birthdates.quests.config.QuestConfig;
import com.birthdates.quests.data.QuestDataService;
import com.birthdates.quests.quest.QuestProgress;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Mock service used for testing.
 */
public class MockQuestDataService extends QuestDataService {
    public MockQuestDataService(QuestConfig questConfig, int maxActiveQuests) {
        super(questConfig, maxActiveQuests);
    }

    @Override
    public CompletableFuture<Void> loadPlayerData(UUID playerId) {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void deleteProgress(UUID playerId, String questId) {

    }

    @Override
    protected void saveProgress(UUID playerId, String questId, QuestProgress progress) {

    }
}
