package com.birthdates.quests.menu.quest.main;

import com.birthdates.quests.QuestPlugin;
import com.birthdates.quests.config.QuestConfig;
import com.birthdates.quests.data.QuestDataService;
import com.birthdates.quests.input.InputService;
import com.birthdates.quests.menu.MenuService;
import com.birthdates.quests.menu.PaginatedMenu;
import com.birthdates.quests.menu.button.ButtonAction;
import com.birthdates.quests.menu.button.MenuButton;
import com.birthdates.quests.menu.quest.main.button.QuestButton;
import com.birthdates.quests.menu.quest.manage.QuestManageMenu;
import com.birthdates.quests.quest.Quest;
import com.birthdates.quests.quest.QuestProgress;
import com.birthdates.quests.quest.QuestStatus;
import com.birthdates.quests.quest.QuestType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class QuestMenu extends PaginatedMenu {

    private final QuestDataService dataService;
    private final QuestConfig questConfig;
    private final boolean admin;
    private List<Quest> quests;

    public QuestMenu(MenuService menuService, QuestDataService dataService, QuestConfig questConfig, boolean admin) {
        super(admin ? "QuestAdminMenu" : "QuestMenu", menuService);
        this.dataService = dataService;
        this.admin = admin;
        this.questConfig = questConfig;
    }

    @Override
    protected void loadButtons(Player player) {
        this.quests = new ArrayList<>(questConfig.getAllQuests());
        this.quests.sort((o1, o2) -> {
            int type = o1.type().compareTo(o2.type());
            if (type == 0) {
                return dataService.getProgress(player.getUniqueId(), o1.id()).status().compareTo(dataService.getProgress(player.getUniqueId(), o2.id()).status());
            }
            return type;
        });
        super.loadButtons(player);
    }

    private void openAdminMenu(Quest quest, Player player) {
        menuService.openMenu(player, new QuestManageMenu(menuService, quest, questConfig));
    }

    private void createQuest(Player player, String id, String type) {
        QuestType questType;
        try {
            questType = QuestType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            QuestPlugin.getInstance().getLanguageService().display(player, "messages.quest.create-admin-invalid-type");
            return;
        }

        questConfig.createQuest(id, questType);
        QuestPlugin.getInstance().getLanguageService().display(player, "messages.quest.create-admin-success");
    }

    private void createQuest(Player player, int slot, ClickType clickType) {
        player.closeInventory();
        InputService.awaitInput(player, "messages.quest.create-admin-id")
                .thenAccept(id -> {
                    QuestType.showQuestTypes(player);
                    InputService.awaitInput(player, null).thenAccept(type -> createQuest(player, id, type));
                });
    }

    @Override
    public ButtonAction getAction(String buttonPath) {
        if (buttonPath.equalsIgnoreCase("create")) {
            return this::createQuest;
        }
        return super.getAction(buttonPath);
    }

    public void onSelect(Quest quest, int slot, Player player, ClickType clickType) {
        if (admin) {
            if (clickType == ClickType.MIDDLE) {
                questConfig.deleteQuest(quest.id());
                refresh(player, true);
                return;
            }
            openAdminMenu(quest, player);
            return;
        }
        QuestProgress progress = dataService.getProgress(player.getUniqueId(), quest.id());
        if (progress.status() == QuestStatus.COMPLETED) {
            return;
        }
        boolean started = progress.status() == QuestStatus.IN_PROGRESS;
        String error = started ? dataService.cancelQuest(player, quest) : dataService.activateQuest(player, quest);
        if (error == null) {
            setTemporaryButton(player, slot, started ? "Quest-Cancelled" : "Quest-Activated", 3, TimeUnit.SECONDS).thenRun(() -> refresh(player));
            return;
        }
        setTemporaryButton(player, slot, error, 3, TimeUnit.SECONDS);
    }

    @Override
    protected MenuButton getButton(Player player, int index) {
        Quest quest = quests.get(index);
        QuestProgress progress = dataService.getProgress(player.getUniqueId(), quest.id());
        String path;
        if (admin) {
            path = "format.admin";
        } else {
            path = "format." + progress.status().name().toLowerCase();
        }
        return new QuestButton(config.getConfigurationSection(path),
                player, null, quest, progress, this);
    }

    @Override
    public int getTotalButtons() {
        return quests.size();
    }
}
