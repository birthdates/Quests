package com.birthdates.quests.menu.lang.select;

import com.birthdates.quests.lang.LanguageService;
import com.birthdates.quests.menu.Menu;
import com.birthdates.quests.menu.MenuService;
import com.birthdates.quests.menu.lang.select.button.SelectButton;
import com.birthdates.quests.menu.lang.view.LanguageMenu;
import org.bukkit.entity.Player;

public class LanguageSelectMenu extends Menu {

    private final LanguageService languageService;
    private final MenuService menuService;

    public LanguageSelectMenu(MenuService menuService, LanguageService languageService) {
        super("LanguageSelectMenu", menuService);
        this.languageService = languageService;
        this.menuService = menuService;
    }

    @Override
    protected void loadButtons(Player player) {
        super.loadButtons(player);

        languageService.getAvailableLanguages().forEach(language ->
                buttons.put(buttons.size(), new SelectButton(config.getConfigurationSection("format.select"), player, language, this))
        );
    }

    public void select(Player player, String language) {
        var menu = new LanguageMenu(menuService, languageService, language);
        menu.setCloseMenu(this);
        menuService.openMenu(player, menu);
    }
}