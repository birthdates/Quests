package com.birthdates.quests.command;

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
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class QuestCommand implements CommandExecutor {
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

    private void openMenu(Player player, boolean admin) {
        Menu menu = new QuestMenu(menuService, dataService, questConfig, admin);
        menuService.openMenu(player, menu);
    }

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

    private void addQuestSign(Player player, int number) {
        Block lookingAt = player.getTargetBlockExact(5);
        if (lookingAt == null || !(lookingAt.getState() instanceof Sign)) {
            languageService.display(player, "messages.admin.sign-not-found");
            return;
        }

        QuestSign sign = new QuestSign(lookingAt.getLocation(), number - 1);
        if (!signService.addSignLocation(sign)) {
            languageService.display(player, "messages.admin.sign-already-exists");
            return;
        }

        languageService.display(player, "messages.admin.sign-added");
        signListener.forceUpdate(sign);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }

        if (args.length >= 1) {
            if (!player.hasPermission("quests.admin")) {
                dataService.alertActiveQuests(player);
                return false;
            }
            switch (args[0].toLowerCase()) {
                case "sign":
                    try {
                        addQuestSign(player, args.length < 2 ? 1 : Integer.parseInt(args[1]));
                    } catch (NumberFormatException ignored) {
                    }
                    return false;
                case "admin":
                    openMenu(player, true);
                    return false;
                case "remove-reward":
                    if (args.length <= 2) {
                        return false;
                    }
                    try {
                        int index = Integer.parseInt(args[2]);
                        onRemoveReward(player, args[1], index);
                    } catch (NumberFormatException ignored) {
                    }
                    return false;
                case "add-reward":
                    onAddReward(player, args[1]);
                    return false;
            }
        }

        openMenu(player, false);
        return false;
    }
}
