package com.birthdates.quests.util;

import com.birthdates.quests.QuestPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Simple but useful locale utilities
 */
public class LocaleUtil {


    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String truncate(String text, int length) {
        return text.length() > length ? text.substring(0, length) + "..." : text;
    }

    public static String localeToName(String locale) {
        return new Locale(locale).getDisplayName();
    }

    public static String formatID(String id) {
        String[] words = id.split("_");
        StringBuilder formatted = new StringBuilder();
        for (String word : words) {
            formatted.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase()).append(" ");
        }
        return formatted.toString().trim();
    }

    public static String formatExpiry(Player player, long expiry) {
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

    public static long parseExpiry(String expiry) {
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

    public static String formatNumber(BigDecimal decimal) {
        if (decimal.scale() <= 0) {
            return String.format("%,d", decimal.intValue());
        }
        return String.format("%,.2f", decimal);
    }

    public static String createProgressBar(double percent) {
        StringBuilder builder = new StringBuilder();
        for (double i = 0; i <= 100; i += 4) {
            builder.append(i <= percent ? ChatColor.GREEN + "|" : ChatColor.RED + "|");
        }
        return builder + " " + ChatColor.GREEN + formatNumber(BigDecimal.valueOf(percent)) + "%";
    }
}
