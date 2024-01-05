package com.birthdates.quests.menu;

import com.birthdates.quests.QuestPlugin;
import com.birthdates.quests.menu.button.MenuButton;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class MenuService implements Listener {
    private final Map<UUID, Menu> inMenu = new HashMap<>();
    private final Set<UUID> exemptClose = new HashSet<>();
    private final Set<Menu> markedForUpdate = new HashSet<>();
    private final YamlConfiguration menuConfig;

    public MenuService(Plugin plugin, YamlConfiguration menuConfig) {
        this.menuConfig = menuConfig;
        Bukkit.getScheduler().runTaskTimer(plugin, this::updateMenus, 20L, 20L);
    }

    public void openMenu(Player player, Menu menu) {
        if (inMenu.containsKey(player.getUniqueId())) {
            exemptClose.add(player.getUniqueId());
            player.closeInventory();
        }
        inMenu.put(player.getUniqueId(), menu);
        menu.open(player);
    }

    protected void markForUpdate(Menu menu) {
        markedForUpdate.add(menu);
    }

    private void updateMenus() {
        markedForUpdate.removeIf(Menu::checkTemporaryButtons);
    }

    public Menu getMenu(Player player) {
        return inMenu.get(player.getUniqueId());
    }

    public void exemptClose(UUID player) {
        exemptClose.add(player);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Menu menu = inMenu.get(player.getUniqueId());
        if (menu == null || exemptClose.remove(player.getUniqueId())) {
            return;
        }
        menu.onClosed(player);
        markedForUpdate.remove(menu);
        inMenu.remove(player.getUniqueId());
        if (menu.getCloseMenu() != null) {
            // Required or will give weird ghost menu.
            Bukkit.getScheduler().runTaskLater(QuestPlugin.getInstance(),
                    () -> openMenu(player, menu.getCloseMenu()), 1L);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Menu menu = inMenu.get(player.getUniqueId());
        if (menu == null || event.getClickedInventory() != player.getOpenInventory().getTopInventory()) {
            System.out.println(menu);
            return;
        }


        event.setCancelled(true);
        MenuButton button = menu.getButton(event.getSlot());
        if (button != null) {
            button.onClick(player, event.getSlot(), event.getClick());
        }
    }

    public YamlConfiguration getMenuConfig() {
        return menuConfig;
    }
}
