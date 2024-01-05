package com.birthdates.quests.menu.quest.main.button;

import com.birthdates.quests.QuestPlugin;
import com.birthdates.quests.lang.LanguageService;
import com.birthdates.quests.menu.button.ButtonAction;
import com.birthdates.quests.menu.button.ConfigButton;
import com.birthdates.quests.menu.quest.main.QuestMenu;
import com.birthdates.quests.quest.Quest;
import com.birthdates.quests.quest.QuestProgress;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.math.RoundingMode;

public class QuestButton extends ConfigButton {

    private final QuestMenu parent;
    private final Quest quest;

    public QuestButton(ConfigurationSection section, Player player, ButtonAction action, Quest quest, QuestProgress progress, QuestMenu parent) {
        super(section, player, action);

        getItem().setType(quest.icon());
        double percent = progress.amount().divide(quest.requiredAmount(), RoundingMode.HALF_EVEN).doubleValue() * 100D;
        String any = QuestPlugin.getInstance().getLanguageService().get("messages.any", player);
        setPlaceholder("%description%", quest.description().split("\\\\n"))
                .setPlaceholder("%name%", LanguageService.formatID(quest.type().name()))
                .setPlaceholder("%required%", LanguageService.formatNumber(quest.requiredAmount()))
                .setPlaceholder("%target%", quest.target() == null ? any : LanguageService.formatID(quest.target()))
                .setPlaceholder("%progress%", LanguageService.formatNumber(progress.amount()))
                .setPlaceholder("%progress_bar%", LanguageService.createProgressBar(percent));
        this.parent = parent;
        this.quest = quest;
    }

    @Override
    public void onClick(Player player, int slot, ClickType clickType) {
        super.onClick(player, slot, clickType);
        parent.onSelect(quest, slot, player, clickType);
    }
}
