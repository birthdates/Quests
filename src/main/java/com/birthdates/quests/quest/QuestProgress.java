package com.birthdates.quests.quest;

import java.math.BigDecimal;

/**
 * A user's quest progress
 *
 * @param amount The amount of progress
 * @param status The status of the quest
 * @param expiry The time the quest expires
 */
public record QuestProgress(BigDecimal amount, QuestStatus status, long expiry) {
    /**
     * Default quest progress (for quests that don't have progress)
     */
    public static final QuestProgress NOT_STARTED = new QuestProgress(BigDecimal.ZERO, QuestStatus.NOT_STARTED, -1);

    public QuestProgress add(BigDecimal amount) {
        return new QuestProgress(this.amount.add(amount), status, expiry);
    }

    public QuestProgress status(QuestStatus status) {
        return new QuestProgress(amount, status, expiry);
    }

    /**
     * Check if the quest has expired
     *
     * @return Whether the quest has expired (will always return false if permanent)
     */
    public boolean isNotExpired() {
        return expiry < 0 || System.currentTimeMillis() <= expiry;
    }

    /**
     * Check if the quest is in progress (not expired)
     *
     * @return Whether the quest is in progress
     */
    public boolean isInProgress() {
        return isNotExpired() && status == QuestStatus.IN_PROGRESS;
    }
}

