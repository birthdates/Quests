package com.birthdates.quests.command;

import com.birthdates.quests.lang.LanguageService;
import com.birthdates.quests.menu.MenuService;
import com.birthdates.quests.menu.lang.select.LanguageSelectMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LanguageCommand implements CommandExecutor {

    private final LanguageService languageService;
    private final MenuService menuService;

    public LanguageCommand(LanguageService languageService, MenuService menuService) {
        this.languageService = languageService;
        this.menuService = menuService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        if (!player.hasPermission("quests.language")) {
            player.sendMessage(LanguageService.color(languageService.get("no_permission", player)));
            return false;
        }
        menuService.openMenu(player, new LanguageSelectMenu(menuService, languageService));
        return false;
    }
}
