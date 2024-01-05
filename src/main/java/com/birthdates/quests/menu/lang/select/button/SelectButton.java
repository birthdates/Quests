package com.birthdates.quests.menu.lang.select.button;

import com.birthdates.quests.menu.button.ConfigButton;
import com.birthdates.quests.menu.lang.select.LanguageSelectMenu;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class SelectButton extends ConfigButton {

    private final LanguageSelectMenu parent;
    private final String language;

    public SelectButton(ConfigurationSection section, Player player, String language, LanguageSelectMenu parent) {
        super(section, player, null);
        setPlaceholder("%language%", language);
        this.parent = parent;
        this.language = language;
    }

    @Override
    public void onClick(Player player, int slot, ClickType clickType) {
        parent.select(player, language);
    }
}
