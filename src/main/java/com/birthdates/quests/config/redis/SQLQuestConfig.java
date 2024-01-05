package com.birthdates.quests.config.redis;

import com.birthdates.quests.QuestPlugin;
import com.birthdates.quests.config.QuestConfig;
import com.birthdates.quests.quest.Quest;
import com.birthdates.quests.quest.QuestType;
import com.birthdates.quests.sql.SQLConnection;
import org.bukkit.Material;

import java.math.BigDecimal;
import java.sql.Array;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SQLQuestConfig implements QuestConfig {
    private final Map<String, Quest> questCache = new ConcurrentHashMap<>();
    private final SQLConnection sql;

    public SQLQuestConfig(SQLConnection sql) {
        this.sql = sql;
    }

    @Override
    public void deleteQuest(String id) {
        sql.getExecutor().execute(() -> {
            try (var connection = sql.getConnection()) {
                connection.createStatement().execute("DELETE FROM quests WHERE id = '" + id + "'");
            } catch (Exception e) {
                throw new IllegalStateException("Failed to delete quest", e);
            }

            QuestPlugin.getInstance().getUpdateListener().sendUpdate(id);
        });
    }

    public void invalidate(String id) {
        questCache.remove(id);
    }

    @Override
    public void saveQuest(Quest quest) {
        String statement = "INSERT INTO quests (id, description, rewards) VALUES (?, ?, ?) ON CONFLICT (id) DO UPDATE SET description = ?, rewards = ?";
        sql.getExecutor().execute(() -> {
            try (var connection = sql.getConnection()) {
                try (var preparedStatement = connection.prepareStatement(statement)) {
                    Array commands = connection.createArrayOf("text", quest.rewardCommands().toArray());
                    preparedStatement.setString(1, quest.id());
                    preparedStatement.setString(2, quest.description());
                    preparedStatement.setArray(3, commands);
                    preparedStatement.setString(4, quest.description());
                    preparedStatement.setArray(5, commands);
                    preparedStatement.execute();
                }
            } catch (Exception e) {
                throw new IllegalStateException("Failed to save quest", e);
            }
            QuestPlugin.getInstance().getUpdateListener().sendUpdate(quest.id());
        });
    }

    @Override
    public void createQuest(String id, QuestType questType) {
        Quest quest = new Quest(id, questType);
        saveQuest(quest);
    }

    @Override
    public Quest getQuest(String id) {
        Quest cached = questCache.get(id);
        if (cached != null) return cached;
        try (var connection = sql.getConnection()) {
            try (var preparedStatement = connection.prepareStatement("SELECT * FROM quests WHERE id = ?")) {
                preparedStatement.setString(1, id);
                try (var resultSet = preparedStatement.executeQuery()) {
                    if (!resultSet.next()) return null;
                    String description = resultSet.getString("description");
                    Array rewards = resultSet.getArray("rewards");
                    List<String> rewardCommands = List.of((String[]) rewards.getArray());
                    BigDecimal requiredAmount = resultSet.getBigDecimal("requiredAmount");
                    QuestType questType = QuestType.values()[resultSet.getInt("questType")];
                    String permission = resultSet.getString("permission");
                    String icon = resultSet.getString("icon");
                    String target = resultSet.getString("target");
                    Quest quest = new Quest(id, Material.valueOf(icon), target, permission, questType, requiredAmount, rewardCommands, description);
                    questCache.put(id, quest);
                    return quest;
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to get quest", e);
        }
    }
}
