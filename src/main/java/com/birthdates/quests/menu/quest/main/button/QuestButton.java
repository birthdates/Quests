package com.birthdates.quests.menu.quest.main.button;

import com.birthdates.quests.menu.button.ButtonAction;
import com.birthdates.quests.menu.button.ConfigButton;
import com.birthdates.quests.menu.quest.main.QuestMenu;
import com.birthdates.quests.quest.Quest;
import com.birthdates.quests.quest.QuestProgress;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class QuestButton extends ConfigButton {
    private final QuestMenu parent;
    private final Quest quest;

    public QuestButton(ConfigurationSection section, Player player, ButtonAction action, Quest quest, QuestProgress progress, QuestMenu parent) {
        super(section, player, action);

        getItem().setType(quest.icon());
        quest.formatButton(player, progress, this, true);
        this.parent = parent;
        this.quest = quest;
    }

    @Override
    public void onClick(Player player, int slot, ClickType clickType) {
        super.onClick(player, slot, clickType);
        parent.onSelect(quest, slot, player, clickType);
    }
}
