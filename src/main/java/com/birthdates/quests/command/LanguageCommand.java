package com.birthdates.quests.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import com.birthdates.quests.lang.LanguageService;
import com.birthdates.quests.menu.MenuService;
import com.birthdates.quests.menu.lang.select.LanguageSelectMenu;
import org.bukkit.entity.Player;

@CommandAlias("language|lang")
public class LanguageCommand extends BaseCommand {

    private final LanguageService languageService;
    private final MenuService menuService;

    public LanguageCommand(LanguageService languageService, MenuService menuService) {
        this.languageService = languageService;
        this.menuService = menuService;
    }

    @Default
    @CommandPermission("quests.admin")
    private void openMenu(Player player) {
        menuService.openMenu(player, new LanguageSelectMenu(menuService, languageService));
    }
}
