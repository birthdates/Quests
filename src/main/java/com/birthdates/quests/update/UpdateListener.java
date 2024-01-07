package com.birthdates.quests.update;

import com.birthdates.quests.config.QuestConfig;
import com.birthdates.quests.lang.LanguageService;

/**
 * Abstract listener for updates cross-server (quest config & language)
 */
public abstract class UpdateListener {

    private final QuestConfig questConfig;
    private final LanguageService languageService;

    public UpdateListener(QuestConfig config, LanguageService languageService) {
        this.questConfig = config;
        this.languageService = languageService;
    }

    /**
     * Send an update to the other servers
     *
     * @param channel The channel to send the update on (type of update)
     * @param value   The value to send
     */
    public abstract void sendUpdate(String channel, String value);

    protected void handleUpdate(String key, String value) {
        // Handle quest or language update
        if (key.equals("QUEST_CONFIG")) {
            questConfig.invalidateCache(value);
            questConfig.getQuest(value);
            return;
        }
        languageService.update(key, value);
    }

    public abstract void unload();
}
