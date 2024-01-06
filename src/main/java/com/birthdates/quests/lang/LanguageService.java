package com.birthdates.quests.lang;

import com.birthdates.quests.util.LocaleUtil;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Service to handle language logic
 */
public interface LanguageService {

    /**
     * Load the default language from a config
     *
     * @param defaultLang Default language config (will have each language as a top-level key)
     */
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

    /**
     * Get a list of formatted strings from a key
     *
     * @param key    Language key
     * @param player Player (will use their language)
     * @return List of formatted strings
     */
    default List<String> getList(String key, Player player) {
        return getList(key, player.locale().getLanguage());
    }

    /**
     * Get a list of formatted strings from a key
     *
     * @param key      Language key
     * @param language Language
     * @return List of formatted strings
     */
    default List<String> getList(String key, String language) {
        return List.of(get(key, language).split("\\\\n"));
    }

    /**
     * Display a formatted language message to a player
     *
     * @param player       Player (will use their language)
     * @param key          Language key
     * @param replacements Replacements for %s (string formatting, see {@link String#format(String, Object...)})
     */
    default void display(Player player, String key, Object... replacements) {
        var list = getList(key, player);
        List<String> placeholders = new ArrayList<>(Stream.of(replacements).map(Object::toString).toList());
        for (String line : list) {
            int index;
            while (!placeholders.isEmpty() && (index = line.indexOf("%s")) >= 0) {
                line = line.substring(0, index) + placeholders.remove(0) + line.substring(index + 2);
            }
            player.sendMessage(LocaleUtil.color(line));
        }
    }

    /**
     * Get a formatted language string from a key
     *
     * @param key    Language key
     * @param player Player (will use their language)
     * @return Formatted string
     */
    default String get(String key, Player player) {
        return get(key, player.locale().getLanguage());
    }

    /**
     * Get a formatted language string from a key
     *
     * @param key      Language key
     * @param language Language
     * @return Formatted string
     */
    String get(String key, String language);

    /**
     * Set a formatted language string
     *
     * @param key      Language key
     * @param value    Formatted language value
     * @param language Language (i.e english)
     * @return {@link CompletableFuture} for when the value is set
     */
    CompletableFuture<Void> set(String key, String value, String language);

    /**
     * Delete a language key
     *
     * @param key      Language key
     * @param language Language (i.e english)
     */
    void delete(String key, String language);

    /**
     * Update/invalidate a language key (used for cross-server messaging)
     *
     * @param key      Language key
     * @param language Language (i.e english)
     */
    void update(String key, String language);

    Collection<String> getAvailableLanguages();

    /**
     * Get map of key -> value for a language
     *
     * @param language Language (i.e english)
     * @return Map of key -> value (i.e "messages.never" -> "Never")
     */
    Map<String, String> getLanguageMap(String language);
}
