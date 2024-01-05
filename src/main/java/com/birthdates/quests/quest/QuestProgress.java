package com.birthdates.quests.quest;

import java.math.BigDecimal;

public record QuestProgress(BigDecimal amount, QuestStatus status) {

    public static final QuestProgress NOT_STARTED = new QuestProgress(BigDecimal.ZERO, QuestStatus.NOT_STARTED);

    public QuestProgress add(BigDecimal amount) {
        return new QuestProgress(this.amount.add(amount), status);
    }

    public QuestProgress status(QuestStatus status) {
        return new QuestProgress(amount, status);
    }
}

