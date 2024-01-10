package com.birthdates.quests.config.impl;

import com.birthdates.quests.QuestPlugin;
import com.birthdates.quests.config.QuestConfig;
import com.birthdates.quests.quest.Quest;
import com.birthdates.quests.quest.QuestType;
import com.birthdates.quests.sql.SQLConnection;
import com.birthdates.quests.update.UpdateType;
import org.bukkit.Material;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SQLQuestConfig implements QuestConfig {
    private final Map<String, Quest> questCache = new ConcurrentHashMap<>();
    private final SQLConnection sql;

    public SQLQuestConfig(SQLConnection sql) {
        this.sql = sql;
        loadData();
    }

    private static void broadcastQuestUpdate(String id) {
        QuestPlugin.getInstance().getUpdateService().sendUpdate(UpdateType.QUEST_CONFIG, id);
    }

    private void loadData() {
        try (var connection = sql.getConnection()) {
            try (var preparedStatement = connection.prepareStatement("SELECT * FROM quests")) {
                try (var resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String id = resultSet.getString("id");
                        questCache.put(id, loadQuest(id, resultSet));
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load quests", e);
        }
    }

    @Override
    public List<Quest> getAllQuests() {
        return questCache.values().stream().toList();
    }

    @Override
    public void deleteQuest(String id) {
        sql.getExecutor().submit(() -> {
            try (var connection = sql.getConnection()) {
                connection.createStatement().execute("DELETE FROM quests WHERE id = '" + id + "'");
            } catch (Exception e) {
                throw new IllegalStateException("Failed to delete quest", e);
            }
            broadcastQuestUpdate(id);
        });
    }

    public void invalidateCache(String id) {
        questCache.remove(id);
    }

    @Override
    public void saveQuest(Quest quest) {
        questCache.put(quest.id(), quest);
        // FIXME: if this becomes too much, it might be better to have individual statements for each field (i.e setQuestDescription)
        String statement = "INSERT INTO quests (id, description, rewards, questType, requiredAmount, permission, icon, target, expiry) VALUES (?, ?, ?, ?, ?, ? ,? ,? , ?) ON CONFLICT (id) DO UPDATE SET description = ?, rewards = ?, questtype = ?, requiredamount = ?, permission = ?, icon = ?, target = ?, expiry = ?";
        sql.getExecutor().submit(() -> {
            try (var connection = sql.getConnection()) {
                try (var preparedStatement = connection.prepareStatement(statement)) {
                    Array commands = connection.createArrayOf("text", quest.rewardCommands().toArray());
                    preparedStatement.setString(1, quest.id());
                    preparedStatement.setString(2, quest.description());
                    preparedStatement.setArray(3, commands);
                    preparedStatement.setInt(4, quest.type().ordinal());
                    preparedStatement.setBigDecimal(5, quest.requiredAmount());
                    preparedStatement.setString(6, quest.permission());
                    preparedStatement.setString(7, quest.icon().name());
                    preparedStatement.setString(8, quest.target());
                    preparedStatement.setLong(9, quest.expiry());
                    preparedStatement.setString(10, quest.description());
                    preparedStatement.setArray(11, commands);
                    preparedStatement.setInt(12, quest.type().ordinal());
                    preparedStatement.setBigDecimal(13, quest.requiredAmount());
                    preparedStatement.setString(14, quest.permission());
                    preparedStatement.setString(15, quest.icon().name());
                    preparedStatement.setString(16, quest.target());
                    preparedStatement.setLong(17, quest.expiry());
                    preparedStatement.execute();
                }
            } catch (Exception e) {
                throw new IllegalStateException("Failed to save quest", e);
            }
            broadcastQuestUpdate(quest.id());
        });
    }

    @Override
    public void createQuest(String id, QuestType questType) {
        Quest quest = new Quest(id, questType);
        saveQuest(quest);
    }

    private Quest loadQuest(String id, ResultSet resultSet) throws SQLException {
        String description = resultSet.getString("description");
        Array rewards = resultSet.getArray("rewards");
        List<String> rewardCommands = List.of((String[]) rewards.getArray());
        BigDecimal requiredAmount = resultSet.getBigDecimal("requiredAmount");
        QuestType questType = QuestType.values()[resultSet.getInt("questType")];
        String permission = resultSet.getString("permission");
        String icon = resultSet.getString("icon");
        String target = resultSet.getString("target");
        long expiry = resultSet.getLong("expiry");
        Quest quest = new Quest(id, Material.valueOf(icon), target, permission, questType, requiredAmount, rewardCommands, description, expiry);
        questCache.put(id, quest);
        return quest;
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
                    return loadQuest(id, resultSet);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to get quest", e);
        }
    }
}
