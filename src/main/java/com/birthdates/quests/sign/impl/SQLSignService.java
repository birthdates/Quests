package com.birthdates.quests.sign.impl;

import com.birthdates.quests.QuestPlugin;
import com.birthdates.quests.sign.QuestSign;
import com.birthdates.quests.sign.SignService;
import com.birthdates.quests.sql.SQLConnection;
import com.birthdates.quests.update.UpdateType;
import com.birthdates.quests.util.LocationUtil;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SQLSignService implements SignService {

    private final Map<String, QuestSign> cachedSigns = new ConcurrentHashMap<>();
    private final SQLConnection sql;

    public SQLSignService(SQLConnection sql) {
        this.sql = sql;
        loadLocations();
    }

    private void loadLocations() {
        String statementStr = "SELECT * FROM quest_signs";
        try (var connection = sql.getConnection()) {
            try (var statement = connection.prepareStatement(statementStr)) {
                try (var resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        loadSign(resultSet);
                    }
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public QuestSign getSign(Location location) {
        return cachedSigns.values().stream().filter(x -> x.location().equals(location)).findFirst().orElse(null);
    }

    @Override
    public Collection<QuestSign> getAllSignLocations() {
        return cachedSigns.values();
    }

    @Override
    public boolean addSignLocation(QuestSign sign) {
        String id = LocationUtil.serializeLocation(sign.location());
        if (cachedSigns.containsKey(id)) {
            return false;
        }
        sql.getExecutor().submit(() -> {
            try (var connection = sql.getConnection()) {
                try (var statement = connection.prepareStatement("INSERT INTO quest_signs (location, questNumber) VALUES (?, ?)")) {
                    statement.setString(1, id);
                    statement.setInt(2, sign.questNumber());
                    statement.execute();
                }
                broadcastUpdate(sign.location());
            } catch (SQLException exception) {
                throw new IllegalStateException("Failed to add sign location", exception);
            }
        });
        return true;
    }

    private void broadcastUpdate(Location location) {
        String id = LocationUtil.serializeLocation(location);
        QuestPlugin.getInstance().getUpdateService().sendUpdate(UpdateType.SIGNS, id);
    }

    @Override
    public boolean removeSignLocation(@NotNull Location location) {
        String id = LocationUtil.serializeLocation(location);
        if (!cachedSigns.containsKey(id)) {
            return false;
        }
        sql.getExecutor().submit(() -> {
            try (var connection = sql.getConnection()) {
                try (var statement = connection.prepareStatement("DELETE FROM quest_signs WHERE location = ?")) {
                    statement.setString(1, id);
                    statement.execute();
                }
                broadcastUpdate(location);
            } catch (SQLException exception) {
                throw new IllegalStateException("Failed to remove sign location", exception);
            }
        });
        return true;
    }

    private void loadSign(ResultSet resultSet) throws SQLException {
        String locationStr = resultSet.getString("location");
        int questNumber = resultSet.getInt("questNumber");
        Location location = LocationUtil.deserializeLocation(locationStr);
        cachedSigns.put(locationStr, new QuestSign(location, questNumber));
    }

    private void loadSign(String id) {
        try (var connection = sql.getConnection()) {
            try (var statement = connection.prepareStatement("SELECT * FROM quest_signs WHERE location = ?")) {
                statement.setString(1, id);
                try (var resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        loadSign(resultSet);
                    }
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void handleUpdate(String id) {
        cachedSigns.remove(id);
        sql.getExecutor().submit(() -> loadSign(id));
    }
}
