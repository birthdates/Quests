package com.birthdates.quests.menu.lang.view.button;

import com.birthdates.quests.input.InputService;
import com.birthdates.quests.lang.LanguageService;
import com.birthdates.quests.menu.button.ConfigButton;
import com.birthdates.quests.menu.lang.view.LanguageMenu;
import com.birthdates.quests.util.LocaleUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class LanguageButton extends ConfigButton {
    private final LanguageService languageService;
    private final String key;
    private final LanguageMenu parent;

    public LanguageButton(ConfigurationSection section, Player player, LanguageService languageService, LanguageMenu parent, String key, String text) {
        super(section, player, null);
        this.languageService = languageService;
        this.parent = parent;
        this.key = key;

        setPlaceholder("%key%", key)
                .setPlaceholder("%text%", LocaleUtil.color(LocaleUtil.truncate(text, 20)));
    }

    @Override
    public void onClick(Player player, int slot, ClickType clickType) {
        super.onClick(player, slot, clickType);
        switch (clickType) {
            case MIDDLE -> {
                languageService.delete(key, parent.getLanguage());
                parent.refresh(player, true);
            }
            case LEFT -> InputService.awaitInput(player, "messages.language.create-entry-value").thenAccept(text -> {
                languageService.set(key, text, parent.getLanguage());
                parent.refresh(player, true);
            });
        }
    }
}
