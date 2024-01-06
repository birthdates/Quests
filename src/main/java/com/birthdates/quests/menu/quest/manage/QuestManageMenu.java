package com.birthdates.quests.menu.quest.manage;

import com.birthdates.quests.QuestPlugin;
import com.birthdates.quests.config.QuestConfig;
import com.birthdates.quests.input.InputService;
import com.birthdates.quests.lang.LanguageService;
import com.birthdates.quests.menu.Menu;
import com.birthdates.quests.menu.MenuService;
import com.birthdates.quests.menu.button.ButtonAction;
import com.birthdates.quests.menu.button.ConfigButton;
import com.birthdates.quests.quest.Quest;
import com.birthdates.quests.quest.QuestProgress;
import com.birthdates.quests.quest.QuestType;
import com.birthdates.quests.util.LocaleUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class QuestManageMenu extends Menu {
    private final String questId;
    private final QuestConfig questConfig;
    private Quest quest;

    public QuestManageMenu(MenuService menuService, Quest quest, QuestConfig questConfig) {
        super("QuestManageMenu", menuService);
        this.questId = quest.id();
        this.questConfig = questConfig;
    }

    @Override
    protected void loadButtons(Player player) {
        quest = questConfig.getQuest(questId);
        super.loadButtons(player);
    }

    private void changeType(Player player) {
        QuestType.showQuestTypes(player);
        InputService.awaitInput(player, null).thenAccept(type -> {
            QuestType questType;
            try {
                questType = QuestType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                QuestPlugin.getInstance().getLanguageService().display(player, "messages.quest.create-admin-invalid-type");
                return;
            }

            questConfig.saveQuest(quest.type(questType));
        });
    }

    private void changeIcon(Player player) {
        InputService.awaitInput(player, "messages.quest.admin-change-icon").thenAccept(iconStr -> {
            Material icon;
            try {
                icon = Material.valueOf(iconStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                QuestPlugin.getInstance().getLanguageService().display(player, "messages.quest.create-admin-invalid-icon");
                return;
            }

            questConfig.saveQuest(quest.icon(icon));
        });
    }

    private void changeDescription(Player player) {
        InputService.awaitInput(player, "messages.quest.admin-change-description").thenAccept(desc -> {
            questConfig.saveQuest(quest.description(desc));
        });
    }

    private void changeRequired(Player player) {
        InputService.awaitInput(player, "messages.quest.admin-change-required").thenAccept(amountStr -> {
            BigDecimal amount;
            try {
                amount = new BigDecimal(amountStr);
            } catch (NumberFormatException e) {
                QuestPlugin.getInstance().getLanguageService().display(player, "messages.quest.create-admin-invalid-number");
                return;
            }

            questConfig.saveQuest(quest.requiredAmount(amount));
        });
    }

    private void changeTarget(Player player) {
        InputService.awaitInput(player, "messages.quest.admin-change-target").thenAccept(target ->
                questConfig.saveQuest(quest.target(target)));
    }

    private void changeRewards(Player player) {
        player.closeInventory();
        LanguageService languageService = QuestPlugin.getInstance().getLanguageService();
        languageService.display(player, "messages.quest.create-admin-edit-rewards");

        for (int i = 0; i < quest.rewardCommands().size(); i++) {
            String rewardCommand = quest.rewardCommands().get(i);
            Component component = Component.text("    " + rewardCommand).color(NamedTextColor.GREEN)
                    .hoverEvent(Component.text(languageService.get("messages.quest.rewards.admin-remove", player)).color(NamedTextColor.RED))
                    .clickEvent(ClickEvent.runCommand("/quests remove-reward " + quest.id() + " " + i));
            player.sendMessage(component);
        }

        Component component = Component.text(languageService.get("messages.quest.rewards.admin-add", player)).color(NamedTextColor.GREEN)
                .hoverEvent(Component.text(languageService.get("messages.quest.rewards.admin-add-click", player)).color(NamedTextColor.GREEN))
                .clickEvent(ClickEvent.runCommand("/quests add-reward " + quest.id()));
        player.sendMessage(component);
    }

    private void changeExpiry(Player player) {
        InputService.awaitInput(player, "messages.quest.admin-change-expiry").thenAccept(expiry ->
                questConfig.saveQuest(quest.expiry(LocaleUtil.parseExpiry(expiry))));
    }

    private void changePermission(Player player) {
        InputService.awaitInput(player, "messages.quest.admin-change-permission").thenAccept(permission ->
                questConfig.saveQuest(quest.permission(permission)));
    }

    @Override
    public String getTitle(Player player) {
        return super.getTitle(player).replaceAll("%id%", quest.id());
    }

    @Override
    protected void editConfigButton(Player player, String path, ConfigButton button) {
        quest.formatButton(player, QuestProgress.NOT_STARTED, button, false);
        if (path.equalsIgnoreCase("icon")) {
            button.getItem().setType(quest.icon());
        }
    }

    @Override
    public ButtonAction getAction(String buttonPath) {
        switch (buttonPath.toLowerCase()) {
            case "type" -> {
                return (player, slot, clickType) -> changeType(player);
            }
            case "icon" -> {
                return (player, slot, clickType) -> changeIcon(player);
            }
            case "description" -> {
                return (player, slot, clickType) -> changeDescription(player);
            }
            case "required" -> {
                return (player, slot, clickType) -> changeRequired(player);
            }
            case "target" -> {
                return (player, slot, clickType) -> changeTarget(player);
            }
            case "rewards" -> {
                return (player, slot, clickType) -> changeRewards(player);
            }
            case "expiry" -> {
                return (player, slot, clickType) -> changeExpiry(player);
            }
            case "permission" -> {
                return (player, slot, clickType) -> changePermission(player);
            }
        }
        return super.getAction(buttonPath);
    }
}
