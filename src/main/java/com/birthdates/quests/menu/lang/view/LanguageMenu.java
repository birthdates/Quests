package com.birthdates.quests.menu.lang.view;

import com.birthdates.quests.input.InputService;
import com.birthdates.quests.lang.LanguageService;
import com.birthdates.quests.menu.MenuService;
import com.birthdates.quests.menu.PaginatedMenu;
import com.birthdates.quests.menu.button.ButtonAction;
import com.birthdates.quests.menu.button.MenuButton;
import com.birthdates.quests.menu.lang.view.button.LanguageButton;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.List;

public class LanguageMenu extends PaginatedMenu {

    private final LanguageService languageService;
    private final List<String> languageKey;
    private final String language;

    public LanguageMenu(MenuService menuService, LanguageService languageService, String language) {
        super("LanguageMenu", menuService);
        this.languageService = languageService;
        this.languageKey = languageService.getLanguageMap(language).keySet().stream().toList();
        this.language = language;
    }

    @Override
    protected MenuButton getButton(Player player, int index) {
        String key = languageKey.get(index);
        return new LanguageButton(config.getConfigurationSection("format.language"), player, languageService, this,
                key, languageService.getLanguageMap(language).get(key));
    }

    private void createLanguageEntry(Player player, int slot, ClickType type) {
        InputService.awaitInput(player, "messages.language.create-entry-key")
                .thenAccept(key -> InputService.awaitInput(player, "messages.language.create-entry-text").thenAccept(text ->
                        languageService.set(language, key, text)
                ));
    }

    @Override
    public ButtonAction getAction(String buttonPath) {
        if (buttonPath.equalsIgnoreCase("create")) {
            return this::createLanguageEntry;
        }
        return super.getAction(buttonPath);
    }

    @Override
    public String getTitle(Player player) {
        return super.getTitle(player).replaceAll("%language%", LanguageService.localeToName(language));
    }

    @Override
    public int getTotalButtons() {
        return languageKey.size();
    }

    public String getLanguage() {
        return language;
    }
}
