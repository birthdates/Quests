package com.birthdates.quests.config;

import com.birthdates.quests.quest.Quest;
import com.birthdates.quests.quest.QuestType;

import java.util.List;

/**
 * Handles quest config logic
 */
public interface QuestConfig {

    void createQuest(String id, QuestType questType);

    void deleteQuest(String id);

    void saveQuest(Quest quest);

    void invalidateCache(String id);

    List<Quest> getAllQuests();

    Quest getQuest(String id);

}
