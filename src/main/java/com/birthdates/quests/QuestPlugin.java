package com.birthdates.quests;

import com.birthdates.quests.command.LanguageCommand;
import com.birthdates.quests.command.QuestCommand;
import com.birthdates.quests.config.QuestConfig;
import com.birthdates.quests.config.impl.MockQuestConfig;
import com.birthdates.quests.config.impl.SQLQuestConfig;
import com.birthdates.quests.data.QuestDataService;
import com.birthdates.quests.data.impl.MockQuestDataService;
import com.birthdates.quests.data.impl.SQLQuestDataService;
import com.birthdates.quests.input.InputService;
import com.birthdates.quests.lang.LanguageService;
import com.birthdates.quests.lang.impl.MockLanguageService;
import com.birthdates.quests.lang.impl.SQLLanguageService;
import com.birthdates.quests.menu.MenuService;
import com.birthdates.quests.quest.QuestListener;
import com.birthdates.quests.sql.SQLConnection;
import com.birthdates.quests.update.UpdateListener;
import com.birthdates.quests.update.impl.MockUpdateListener;
import com.birthdates.quests.update.impl.RedisUpdateListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Plugin entry point
 */
public class QuestPlugin extends JavaPlugin {
    private static QuestPlugin instance;
    private QuestDataService dataService;
    private SQLConnection sqlConnection;
    private LanguageService languageService;
    private UpdateListener updateListener;
    private MenuService menuService;
    private QuestConfig questConfig;

    /**
     * Get the singleton plugin instance
     *
     * @return The plugin instance
     */
    public static QuestPlugin getInstance() {
        return instance;
    }

    /**
     * Check if the plugin is running in a test environment
     *
     * @return True if the plugin is running in a test environment
     */
    private static boolean isTestEnvironment() {
        try {
            Class.forName("org.junit.jupiter.api.Test");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        boolean testEnvironment = isTestEnvironment();

        // Load config
        if (testEnvironment) {
            questConfig = new MockQuestConfig();
        } else {
            sqlConnection = new SQLConnection(getLogger(), getConfig().getConfigurationSection("Postgres"));
            questConfig = new SQLQuestConfig(sqlConnection);
        }

        // Load language service
        YamlConfiguration defaultLang = getConfig("default_lang.yml");
        if (testEnvironment) {
            languageService = new MockLanguageService(defaultLang);
        } else {
            languageService = new SQLLanguageService(defaultLang, sqlConnection);
        }


        // Load update listener
        if (testEnvironment) {
            updateListener = new MockUpdateListener(questConfig, languageService);
        } else {
            updateListener = new RedisUpdateListener(questConfig, getConfig().getConfigurationSection("Redis"), languageService);
        }

        menuService = new MenuService(this, getConfig("menus.yml"));

        // Load data service
        int maxActiveQuests = getConfig().getInt("Max-Active-Quests");
        if (testEnvironment) {
            dataService = new MockQuestDataService(getLogger(), questConfig, maxActiveQuests);
        } else {
            dataService = new SQLQuestDataService(getLogger(), questConfig, getConfig().getInt("Max-Active-Quests"), sqlConnection);
        }

        // Register events and commands
        Bukkit.getPluginManager().registerEvents(new QuestListener(dataService), this);
        Bukkit.getPluginManager().registerEvents(new InputService(), this);
        Bukkit.getPluginManager().registerEvents(menuService, this);
        Bukkit.getPluginManager().registerEvents(dataService, this);

        getCommand("language").setExecutor(new LanguageCommand(languageService, menuService));
        getCommand("quest").setExecutor(new QuestCommand(questConfig, languageService, menuService, dataService));
    }

    /**
     * Load a YML config from file path
     *
     * @param filePath The file path
     * @return {@link YamlConfiguration}
     */
    public YamlConfiguration getConfig(String filePath) {
        File file = new File(getDataFolder(), filePath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            InputStream resource = getClassLoader().getResourceAsStream(filePath);
            try {
                Files.copy(resource, file.toPath());
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public void onDisable() {
        updateListener.unload();
        dataService.unload();
        if (sqlConnection != null)
            sqlConnection.unload();
        instance = null;
    }

    public MenuService getMenuService() {
        return menuService;
    }

    /**
     * Get the update listener (used for receiving and sending updates to other server instances)
     *
     * @return {@link UpdateListener}
     */
    public UpdateListener getUpdateListener() {
        return updateListener;
    }

    public LanguageService getLanguageService() {
        return languageService;
    }

    public QuestConfig getQuestConfig() {
        return questConfig;
    }

    public QuestDataService getDataService() {
        return dataService;
    }
}
