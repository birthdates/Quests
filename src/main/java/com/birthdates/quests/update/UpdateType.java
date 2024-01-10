package com.birthdates.quests.update;

public enum UpdateType {
    QUEST_CONFIG,
    LANGUAGE,
    SIGNS;

    public String getChannel() {
        return name();
    }
}
