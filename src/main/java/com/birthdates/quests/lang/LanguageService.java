package com.birthdates.quests.lang;

import com.birthdates.quests.QuestPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

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
            formatted.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase()).append(" ");
        }
        return formatted.toString().trim();
    }

    default void loadDefaultLang(YamlConfiguration defaultLang) {
        for (String language : defaultLang.getKeys(false)) {
            for (String key : defaultLang.getConfigurationSection(language).getKeys(true)) {
                if (!defaultLang.isString(language + "." + key)) {
                    continue;
                }
                set(key, defaultLang.getString(language + "." + key), language);
            }
        }
    }

    static String formatExpiry(Player player, long expiry) {
        if (expiry < 0) {
            return QuestPlugin.getInstance().getLanguageService().get("messages.never", player);
        }
        long seconds = expiry / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        if (days > 0) {
            return days + "d";
        }
        if (hours > 0) {
            return hours + "h";
        }
        if (minutes > 0) {
            return minutes + "m";
        }
        return seconds + "s";
    }

    static long parseExpiry(String expiry) {
        if (expiry.equalsIgnoreCase("perm")) {
            return -1L;
        }
        long time = Long.parseLong(expiry.substring(0, expiry.length() - 1));
        return switch (expiry.substring(expiry.length() - 1)) {
            case "d" -> time * 24 * 60 * 60 * 1000;
            case "h" -> time * 60 * 60 * 1000;
            case "m" -> time * 60 * 1000;
            case "s" -> time * 1000;
            default -> throw new IllegalArgumentException("Invalid expiry format");
        };
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

    default void display(Player player, String key, Object... replacements) {
        var list = getList(key, player);
        List<String> placeholders = new ArrayList<>(Stream.of(replacements).map(Object::toString).toList());
        for (String line : list) {
            int index;
            while (!placeholders.isEmpty() && (index = line.indexOf("%s")) >= 0) {
                line = line.substring(0, index) + placeholders.remove(0) + line.substring(index + 2);
            }
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
