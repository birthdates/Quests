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

/**
 * Service to handle menu logic
 */
public class MenuService implements Listener {
    /**
     * Used to prevent the menu from closing when opening a new one
     */
    private final Set<UUID> exemptClose = new HashSet<>();
    private final Map<UUID, Menu> inMenu = new HashMap<>();
    private final Set<Menu> markedForUpdate = new HashSet<>();
    private final YamlConfiguration menuConfig;

    public MenuService(Plugin plugin, YamlConfiguration menuConfig) {
        this.menuConfig = menuConfig;
        Bukkit.getScheduler().runTaskTimer(plugin, this::updateMenus, 20L, 20L);
    }

    /**
     * Open a menu for a player
     *
     * @param player Target player
     * @param menu   Menu to open
     */
    public void openMenu(Player player, Menu menu) {
        if (inMenu.containsKey(player.getUniqueId())) {
            exemptClose.add(player.getUniqueId());
            player.closeInventory();
        }
        inMenu.put(player.getUniqueId(), menu);
        menu.open(player);
    }

    /**
     * Mark a menu for update (temporary buttons)
     *
     * @param menu Menu to update
     */
    protected void markForUpdate(Menu menu) {
        markedForUpdate.add(menu);
    }

    /**
     * Update all menus that have temporary buttons
     */
    private void updateMenus() {
        markedForUpdate.removeIf(Menu::checkTemporaryButtons);
    }

    /**
     * Get the menu a player is currently in
     *
     * @param player Target player
     * @return Menu the player is in
     */
    public Menu getMenu(Player player) {
        return inMenu.get(player.getUniqueId());
    }

    /**
     * Prevent a menu from closing when opening a new one
     *
     * @param player Target player
     */
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

        // Handle menu close
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
            return;
        }

        // Handle menu button click
        event.setCancelled(true);
        MenuButton button = menu.getButton(event.getSlot());
        if (button != null) {
            button.onClick(player, event.getSlot(), event.getClick());
        }
    }

    /**
     * Get the {@link YamlConfiguration} used for menus
     *
     * @return {@link YamlConfiguration} for menus
     */
    public YamlConfiguration getMenuConfig() {
        return menuConfig;
    }
}
