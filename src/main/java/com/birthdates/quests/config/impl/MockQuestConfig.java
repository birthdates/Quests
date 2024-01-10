package com.birthdates.quests.config.impl;

import com.birthdates.quests.config.QuestConfig;
import com.birthdates.quests.quest.Quest;
import com.birthdates.quests.quest.QuestType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MockQuestConfig implements QuestConfig {
    private final Map<String, Quest> memoryCache = new HashMap<>();


    @Override
    public void createQuest(String id, QuestType questType) {
        Quest quest = new Quest(id, questType);
        memoryCache.put(id, quest);
    }

    @Override
    public CompletableFuture<Void> deleteQuest(String id) {
        memoryCache.remove(id);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void saveQuest(Quest quest) {
        memoryCache.put(quest.id(), quest);
    }

    @Override
    public void invalidateCache(String id) {
        // no-op
    }

    @Override
    public List<Quest> getAllQuests() {
        return memoryCache.values().stream().toList();
    }

    @Override
    public Quest getQuest(String id) {
        return memoryCache.get(id);
    }
}
