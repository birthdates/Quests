package com.birthdates.quests.menu.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public interface ButtonAction {
    void onClick(Player player, int slot, ClickType clickType);
}
