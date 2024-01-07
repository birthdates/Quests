package com.birthdates.quests.update.impl;

import com.birthdates.quests.config.QuestConfig;
import com.birthdates.quests.lang.LanguageService;
import com.birthdates.quests.update.UpdateListener;

/**
 * Mock update listener for testing
 */
public class MockUpdateListener extends UpdateListener {
    public MockUpdateListener(QuestConfig config, LanguageService languageService) {
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
