package com.birthdates.quests.config;

import com.birthdates.quests.quest.Quest;
import com.birthdates.quests.quest.QuestType;

public interface QuestConfig {

    void createQuest(String id, QuestType questType);

    void deleteQuest(String id);

    void saveQuest(Quest quest);

    void invalidate(String id);

    Quest getQuest(String id);

}
