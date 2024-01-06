package com.birthdates.quests.menu.button;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public interface MenuButton {
    ItemStack getItem();

    void onClick(Player player, int slot, ClickType clickType);
}
