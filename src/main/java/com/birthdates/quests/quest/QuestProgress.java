package com.birthdates.quests.quest;

import java.math.BigDecimal;

public record QuestProgress(BigDecimal amount, QuestStatus status) {
}
