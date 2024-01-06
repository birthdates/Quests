package com.birthdates.quests.data.impl;


import com.birthdates.quests.config.QuestConfig;
import com.birthdates.quests.data.QuestDataService;
import com.birthdates.quests.quest.QuestProgress;
import com.birthdates.quests.quest.QuestStatus;
import com.birthdates.quests.sql.SQLConnection;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class SQLQuestDataService extends QuestDataService {

    private final SQLConnection sql;

    public SQLQuestDataService(Logger logger, QuestConfig questConfig, int maxActiveQuests, SQLConnection sql) {
        super(logger, questConfig, maxActiveQuests);
        this.sql = sql;
    }

    @Override
    public CompletableFuture<Void> loadPlayerData(UUID playerId) {
        String statement = "SELECT * FROM quest_progress WHERE userId = ?";
        return CompletableFuture.runAsync(() -> {
            try (var connection = sql.getConnection()) {
                try (var preparedStatement = connection.prepareStatement(statement)) {
                    preparedStatement.setObject(1, playerId);
                    try (var resultSet = preparedStatement.executeQuery()) {
                        while (resultSet.next()) {
                            String questId = resultSet.getString("questId");
                            BigDecimal value = resultSet.getBigDecimal("value");
                            int status = resultSet.getInt("status");
                            long expiry = resultSet.getLong("expiry");
                            userQuestProgress.computeIfAbsent(playerId, uuid -> new ConcurrentHashMap<>())
                                    .put(questId, new QuestProgress(value, QuestStatus.values()[status], expiry));
                        }
                    }
                }
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load quest progress", e);
            }
        }, sql.getExecutor());
    }

    @Override
    public void deleteProgress(UUID playerId, String questId) {
        String statement = "DELETE FROM quest_progress WHERE userId = ? AND questId = ?";
        sql.getExecutor().execute(() -> {
            try (var connection = sql.getConnection()) {
                try (var preparedStatement = connection.prepareStatement(statement)) {
                    preparedStatement.setObject(1, playerId);
                    preparedStatement.setString(2, questId);
                    preparedStatement.execute();
                }
            } catch (Exception e) {
                throw new IllegalStateException("Failed to delete quest progress", e);
            }
        });
    }

    @Override
    protected void saveProgress(UUID playerId, String questId, QuestProgress progress) {
        String statement = "INSERT INTO quest_progress (userId, questId, value, status, expiry) VALUES (?, ?, ?, ?, ?) ON CONFLICT (userId, questId) DO UPDATE SET value = ?, status = ?, expiry = ?";
        sql.getExecutor().execute(() -> {
            try (var connection = sql.getConnection()) {
                try (var preparedStatement = connection.prepareStatement(statement)) {
                    preparedStatement.setObject(1, playerId);
                    preparedStatement.setString(2, questId);
                    preparedStatement.setBigDecimal(3, progress.amount());
                    preparedStatement.setInt(4, progress.status().ordinal());
                    preparedStatement.setLong(5, progress.expiry());
                    preparedStatement.setBigDecimal(6, progress.amount());
                    preparedStatement.setInt(7, progress.status().ordinal());
                    preparedStatement.setLong(8, progress.expiry());
                    preparedStatement.execute();
                }
            } catch (Exception e) {
                throw new IllegalStateException("Failed to save quest progress", e);
            }
        });
    }
}
