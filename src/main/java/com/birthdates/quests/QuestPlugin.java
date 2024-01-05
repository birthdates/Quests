package com.birthdates.quests;

import com.birthdates.quests.command.LanguageCommand;
import com.birthdates.quests.config.QuestConfig;
import com.birthdates.quests.config.redis.SQLQuestConfig;
import com.birthdates.quests.data.QuestDataService;
import com.birthdates.quests.data.impl.SQLQuestDataService;
import com.birthdates.quests.input.InputService;
import com.birthdates.quests.lang.LanguageService;
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

public class QuestPlugin extends JavaPlugin {

    private static QuestPlugin instance;
    private QuestDataService dataService;
    private SQLConnection sqlConnection;
    private LanguageService languageService;

    private UpdateListener updateListener;
    private MenuService menuService;

    public static QuestPlugin getInstance() {
        return instance;
    }

    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        sqlConnection = new SQLConnection(getConfig().getConfigurationSection("Postgres"));
        QuestConfig questConfig = new SQLQuestConfig(sqlConnection);
        languageService = new SQLLanguageService(getConfig("default_lang.yml"), sqlConnection);
        updateListener = new UpdateListener(questConfig, getConfig().getConfigurationSection("Redis"), languageService);
        dataService = new SQLQuestDataService(questConfig, getConfig().getInt("Max-Active-Quests"), sqlConnection);
        menuService = new MenuService(getConfig("menus.yml"));

        Bukkit.getPluginManager().registerEvents(new QuestListener(dataService), this);
        Bukkit.getPluginManager().registerEvents(new InputService(), this);
        Bukkit.getPluginManager().registerEvents(menuService, this);
        Bukkit.getPluginManager().registerEvents(dataService, this);

        getCommand("language").setExecutor(new LanguageCommand(languageService, menuService));
    }

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
        sqlConnection.unload();
        instance = null;
    }

    public MenuService getMenuService() {
        return menuService;
    }

    public UpdateListener getUpdateListener() {
        return updateListener;
    }

    public LanguageService getLanguageService() {
        return languageService;
    }
}
