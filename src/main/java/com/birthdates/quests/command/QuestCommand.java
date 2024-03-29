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
import com.birthdates.quests.sign.QuestSign;
import com.birthdates.quests.sign.SignListener;
import com.birthdates.quests.sign.SignService;
import com.birthdates.quests.util.LocaleUtil;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.ArrayList;

@CommandAlias("quest|quests")
public class QuestCommand extends BaseCommand {
    private final QuestConfig questConfig;
    private final LanguageService languageService;
    private final MenuService menuService;
    private final QuestDataService dataService;
    private final SignService signService;
    private final SignListener signListener;

    public QuestCommand(QuestConfig questConfig, LanguageService languageService, MenuService menuService, QuestDataService dataService, SignService signService, SignListener signListener) {
        this.questConfig = questConfig;
        this.languageService = languageService;
        this.menuService = menuService;
        this.dataService = dataService;
        this.signService = signService;
        this.signListener = signListener;
    }

    @Default
    private void openMenu(Player player) {
        openMenu(player, false);
    }

    @Subcommand("admin")
    @CommandPermission("quests.admin")
    private void adminMenu(Player player) {
        openMenu(player, true);
    }

    private void openMenu(Player player, boolean admin) {
        Menu menu = new QuestMenu(menuService, dataService, questConfig, admin);
        menuService.openMenu(player, menu);
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
        player.sendMessage(LocaleUtil.color(msg));
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
            player.sendMessage(LocaleUtil.color(msg));
        });
    }

    @Subcommand("alerts")
    private void questAlerts(Player player) {
        if (dataService.getActiveQuests(player).isEmpty()) {
            languageService.display(player, "messages.no-quests-active");
            return;
        }
        dataService.alertActiveQuests(player);
    }

    @Subcommand("sign")
    @CommandPermission("quests.admin")
    private void addQuestSign(Player player, int number) {
        Block lookingAt = player.getTargetBlockExact(5);
        if (lookingAt == null || !(lookingAt.getState() instanceof Sign)) {
            languageService.display(player, "messages.admin.sign-not-found");
            return;
        }

        QuestSign sign = new QuestSign(lookingAt.getLocation(), Math.max(number - 1, 0));
        if (!signService.addSignLocation(sign)) {
            languageService.display(player, "messages.admin.sign-already-exists");
            return;
        }

        languageService.display(player, "messages.admin.sign-added");
        signListener.forceUpdate(sign);
    }
}
