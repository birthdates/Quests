package com.birthdates.quests;

import com.birthdates.quests.command.LanguageCommand;
import com.birthdates.quests.command.QuestCommand;
import com.birthdates.quests.config.QuestConfig;
import com.birthdates.quests.config.redis.MockQuestConfig;
import com.birthdates.quests.config.redis.SQLQuestConfig;
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
import com.birthdates.quests.updates.UpdateListener;
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

    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        boolean testEnvironment = isTestEnvironment();

        // Load services
        if (testEnvironment) {
            questConfig = new MockQuestConfig();
        } else {
            sqlConnection = new SQLConnection(getLogger(), getConfig().getConfigurationSection("Postgres"));
            questConfig = new SQLQuestConfig(sqlConnection);
        }

        YamlConfiguration defaultLang = getConfig("default_lang.yml");
        if (testEnvironment) {
            languageService = new MockLanguageService(defaultLang);
        } else {
            languageService = new SQLLanguageService(defaultLang, sqlConnection);
        }
        updateListener = new UpdateListener(questConfig, getConfig().getConfigurationSection("Redis"), languageService);

        int maxActiveQuests = getConfig().getInt("Max-Active-Quests");
        if (testEnvironment) {
            dataService = new MockQuestDataService(questConfig, maxActiveQuests);
        } else {
            dataService = new SQLQuestDataService(questConfig, getConfig().getInt("Max-Active-Quests"), sqlConnection);
        }

        menuService = new MenuService(this, getConfig("menus.yml"));

        // Register events and commands
        Bukkit.getPluginManager().registerEvents(new QuestListener(dataService), this);
        Bukkit.getPluginManager().registerEvents(new InputService(), this);
        Bukkit.getPluginManager().registerEvents(menuService, this);
        Bukkit.getPluginManager().registerEvents(dataService, this);

        getCommand("language").setExecutor(new LanguageCommand(languageService, menuService));
        getCommand("quest").setExecutor(new QuestCommand(questConfig, languageService, menuService, dataService));
    }

    private static boolean isTestEnvironment() {
        try {
            Class.forName("org.junit.jupiter.api.Test");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
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

    /**
     * Get the menu service (used for opening/creating menus)
     *
     * @return {@link MenuService}
     */
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

    /**
     * Get the language service (used for loading and retrieving language strings)
     *
     * @return {@link LanguageService}
     */
    public LanguageService getLanguageService() {
        return languageService;
    }

    public SQLConnection getSqlConnection() {
        return sqlConnection;
    }

    public QuestConfig getQuestConfig() {
        return questConfig;
    }
}
