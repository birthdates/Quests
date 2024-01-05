package com.birthdates.quests.lang;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
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

    static String formatID(String id) {
        String[] words = id.split("_");
        StringBuilder formatted = new StringBuilder();
        for (String word : words) {
            formatted.append(word.substring(0, 1).toUpperCase()).append(word.substring(1)).append(" ");
        }
        return formatted.toString().trim();
    }

    static String formatNumber(BigDecimal decimal) {
        if (decimal.scale() <= 0) {
            return String.format("%,d", decimal.intValue());
        }
        return String.format("%,.2f", decimal);
    }

    static String createProgressBar(double percent) {
        StringBuilder builder = new StringBuilder();
        for (double i = 0; i <= 100; i += 4) {
            builder.append(i <= percent ? ChatColor.GREEN + "|" : ChatColor.RED + "|");
        }
        return builder + " " + ChatColor.GREEN + formatNumber(BigDecimal.valueOf(percent)) + "%";
    }

    default List<String> getList(String key, Player player) {
        return getList(key, player.locale().getLanguage());
    }

    default List<String> getList(String key, String language) {
        return List.of(get(key, language).split("\\\\n"));
    }

    default void display(Player player, String key) {
        var list = getList(key, player);
        for (String line : list) {
            player.sendMessage(color(line));
        }
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
