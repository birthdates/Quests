package com.birthdates.quests.update;

import com.birthdates.quests.config.QuestConfig;
import com.birthdates.quests.lang.LanguageService;
import com.birthdates.quests.sign.SignService;

/**
 * Abstract listener for updates cross-server (quest config & language)
 */
public abstract class UpdateService {

    private final QuestConfig questConfig;
    private final LanguageService languageService;
    private final SignService signService;

    public UpdateService(QuestConfig config, LanguageService languageService, SignService signService) {
        this.questConfig = config;
        this.languageService = languageService;
        this.signService = signService;
    }

    /**
     * Send an update to the other servers
     *
     * @param channel The channel to send the update on (type of update)
     * @param value   The value to send
     */
    public abstract void sendUpdate(UpdateType channel, String value);

    protected void handleUpdate(String key, String value) {
        // Handle quest or language update
        if (key.equals(UpdateType.QUEST_CONFIG.getChannel())) {
            questConfig.invalidateCache(value);
            questConfig.getQuest(value);
            return;
        }

        UpdateType type = UpdateType.valueOf(key);
        switch (type) {
            case QUEST_CONFIG -> {
                questConfig.invalidateCache(value);
                questConfig.getQuest(value);
            }
            case SIGNS -> signService.handleUpdate(value);
            default -> languageService.update(key, value); // Language requires key value pair
        }

    }

    public abstract void unload();
}
