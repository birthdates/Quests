package com.birthdates.quests.update.impl;

import com.birthdates.quests.config.QuestConfig;
import com.birthdates.quests.lang.LanguageService;
import com.birthdates.quests.update.UpdateService;

/**
 * Mock update listener for testing
 */
public class MockUpdateService extends UpdateService {
    public MockUpdateService(QuestConfig config, LanguageService languageService) {
        super(config, languageService);
    }

    @Override
    public void sendUpdate(String channel, String value) {
        handleUpdate(channel, value);
    }

    @Override
    public void unload() {
        // no-op
    }
}
