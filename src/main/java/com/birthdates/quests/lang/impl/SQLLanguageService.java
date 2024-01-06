package com.birthdates.quests.lang.impl;

import com.birthdates.quests.QuestPlugin;
import com.birthdates.quests.lang.LanguageService;
import com.birthdates.quests.sql.SQLConnection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SQLLanguageService implements LanguageService {

    private final SQLConnection sql;
    private final Map<String, Map<String, String>> languageCache = new HashMap<>();

    public SQLLanguageService(YamlConfiguration defaultLang, SQLConnection sql) {
        this.sql = sql;
        loadData();
        if (languageCache.isEmpty()) {
            loadDefaultLang(defaultLang);
        }
    }

    private void loadData() {
        try (var connection = sql.getConnection()) {
            try (var preparedStatement = connection.prepareStatement("SELECT * FROM language")) {
                try (var resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        String key = resultSet.getString("key");
                        String value = resultSet.getString("text");
                        String language = resultSet.getString("language");
                        updateCache(key, value, language, false);
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load language", e);
        }
    }

    @Override
    public Collection<String> getLanguages() {
        return languageCache.keySet();
    }

    @Override
    public Map<String, String> getLanguageMap(String language) {
        return languageCache.get(language.toLowerCase());
    }

    @Override
    public void update(String key, String language) {
        Map<String, String> languageMap = languageCache.get(language.toLowerCase());
        if (languageMap != null) {
            languageMap.remove(key);
        }
        get(key, language);
    }

    @Override
    public String get(String key, String language) {
        Map<String, String> languageMap = languageCache.get(language.toLowerCase());
        if (languageMap == null) {
            if (language.contains("_")) {
                return get(key, language.split("_")[0].toLowerCase());
            }

            if (languageCache.isEmpty()) {
                return key;
            }

            return get(key, !language.equals("en") ? "en" : languageCache.keySet().stream().findFirst().orElse(null));
        }
        String value = languageMap.get(key);
        if (value == null) {
            return key;
        }
        return value;
    }

    private void updateCache(String key, String value, String language, boolean broadcast) {
        Map<String, String> languageMap = languageCache.computeIfAbsent(language.toLowerCase(), k -> new HashMap<>());
        languageMap.put(key, value);
        if (broadcast) {
            QuestPlugin.getInstance().getUpdateListener().sendUpdate(language, key);
        }
    }

    @Override
    public void delete(String key, String language) {
        sql.getExecutor().execute(() -> {
            try (var connection = sql.getConnection()) {
                try (var preparedStatement = connection.prepareStatement("DELETE FROM language WHERE key = ? AND language = ?")) {
                    preparedStatement.setString(1, key);
                    preparedStatement.setString(2, language.toLowerCase());
                    preparedStatement.execute();
                }
            } catch (Exception e) {
                throw new IllegalStateException("Failed to delete language", e);
            }
            updateCache(key, null, language, true);
        });
    }

    @Override
    public CompletableFuture<Void> set(String key, String value, String language) {
        return CompletableFuture.runAsync(() -> {
            try (var connection = sql.getConnection()) {
                try (var preparedStatement = connection.prepareStatement("INSERT INTO language (key, text, language) VALUES (?, ?, ?) ON CONFLICT (key, language) DO UPDATE SET text = ?")) {
                    preparedStatement.setString(1, key);
                    preparedStatement.setString(2, value);
                    preparedStatement.setString(3, language.toLowerCase());
                    preparedStatement.setString(4, value);
                    preparedStatement.execute();
                }
            } catch (Exception e) {
                throw new IllegalStateException("Failed to save language", e);
            }
            updateCache(key, value, language, true);
        }, sql.getExecutor());
    }

}
