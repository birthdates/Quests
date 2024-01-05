package com.birthdates.quests.lang;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface LanguageService {

    static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    static String truncate(String text, int length) {
        return text.length() > length ? text.substring(0, length) + "..." : text;
    }

    static String localeToName(String locale) {
        return new Locale(locale).getDisplayName();
    }

    default List<String> getList(String key, Player player) {
        return getList(key, player.locale().getLanguage());
    }

    default List<String> getList(String key, String language) {
        return List.of(get(key, language).split("\\\\n"));
    }

    default String get(String key, Player player) {
        return get(key, player.locale().getLanguage());
    }

    String get(String key, String language);

    CompletableFuture<Void> set(String key, String value, String language);

    void delete(String key, String language);

    void update(String key, String language);

    Collection<String> getLanguages();

    Map<String, String> getLanguageMap(String language);
}
