package com.birthdates.quests.update.impl;

import com.birthdates.quests.config.QuestConfig;
import com.birthdates.quests.lang.LanguageService;
import com.birthdates.quests.sign.SignService;
import com.birthdates.quests.update.UpdateService;
import com.birthdates.quests.update.UpdateType;

/**
 * Mock update listener for testing
 */
public class MockUpdateService extends UpdateService {
    public MockUpdateService(QuestConfig config, LanguageService languageService, SignService signService) {
        super(config, languageService, signService);
    }

    @Override
    public void sendUpdate(UpdateType type, String value) {
        handleUpdate(type.getChannel(), value);
    }

    @Override
    public void unload() {
        // no-op
    }
}
