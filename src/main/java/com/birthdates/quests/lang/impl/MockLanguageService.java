package com.birthdates.quests.lang.impl;

import com.birthdates.quests.lang.LanguageService;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Mock service used for testing.
 */
public class MockLanguageService implements LanguageService {

    private final Map<String, Map<String, String>> languageCache = new HashMap<>();

    public MockLanguageService(YamlConfiguration defaultLang) {
        loadDefaultLang(defaultLang);
    }

    @Override
    public String get(String key, String language) {
        return languageCache.get(language.toLowerCase()).get(key);
    }

    @Override
    public CompletableFuture<Void> set(String key, String value, String language) {
        languageCache.computeIfAbsent(language.toLowerCase(), k -> new HashMap<>()).put(key, value);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void delete(String key, String language) {
        var languageMap = languageCache.get(language.toLowerCase());
        if (languageMap != null) {
            languageMap.remove(key);
        }
    }

    @Override
    public void update(String key, String language) {
    }

    @Override
    public Collection<String> getAvailableLanguages() {
        return languageCache.keySet();
    }

    @Override
    public Map<String, String> getLanguageMap(String language) {
        return languageCache.get(language.toLowerCase());
    }
}
