package com.birthdates.quests.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import com.birthdates.quests.config.QuestConfig;
import com.birthdates.quests.data.QuestDataService;
import com.birthdates.quests.input.InputService;
import com.birthdates.quests.lang.LanguageService;
import com.birthdates.quests.menu.Menu;
import com.birthdates.quests.menu.MenuService;
import com.birthdates.quests.menu.quest.main.QuestMenu;
import com.birthdates.quests.quest.Quest;
import org.bukkit.entity.Player;

import java.util.ArrayList;

@CommandAlias("quest|quests|q")
public class QuestCommand extends BaseCommand {

    private final QuestConfig questConfig;
    private final LanguageService languageService;
    private final MenuService menuService;
    private final QuestDataService dataService;

    public QuestCommand(QuestConfig questConfig, LanguageService languageService, MenuService menuService, QuestDataService dataService) {
        this.questConfig = questConfig;
        this.languageService = languageService;
        this.menuService = menuService;
        this.dataService = dataService;
    }

    @Default
    public void openMenu(Player player) {
        openMenu(player, false);
    }

    private void openMenu(Player player, boolean admin) {
        Menu menu = new QuestMenu(menuService, dataService, questConfig, admin);
        menuService.openMenu(player, menu);
    }

    @Subcommand("admin")
    @CommandPermission("quests.admin")
    public void onAdmin(Player player) {
        openMenu(player, true);
    }

    @Subcommand("remove-reward")
    @CommandPermission("quests.admin")
    public void onRemoveReward(Player player, String questId, int index) {
        Quest quest = questConfig.getQuest(questId);
        if (quest == null || quest.rewardCommands().size() <= index) {
            return;
        }

        var newCommands = new ArrayList<>(quest.rewardCommands());
        newCommands.remove(index);
        questConfig.saveQuest(quest.rewardCommands(newCommands));

        String msg = languageService.get("messages.quest.admin-removed-reward", player);
        player.sendMessage(LanguageService.color(msg));
    }

    @Subcommand("add-reward")
    @CommandPermission("quests.admin")
    public void onAddReward(Player player, String questId) {
        Quest quest = questConfig.getQuest(questId);
        if (quest == null) {
            return;
        }

        InputService.awaitInput(player, "messages.quest.admin-add-reward").thenAccept(command -> {
            var newCommands = new ArrayList<>(quest.rewardCommands());
            newCommands.add(command);
            questConfig.saveQuest(quest.rewardCommands(newCommands));

            String msg = languageService.get("messages.quest.admin-added-reward", player);
            player.sendMessage(LanguageService.color(msg));
        });
    }
}
