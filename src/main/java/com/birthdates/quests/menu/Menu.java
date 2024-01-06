package com.birthdates.quests.menu;

import com.birthdates.quests.QuestPlugin;
import com.birthdates.quests.lang.LanguageService;
import com.birthdates.quests.menu.button.ButtonAction;
import com.birthdates.quests.menu.button.ConfigButton;
import com.birthdates.quests.menu.button.MenuButton;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * An abstract menu
 */
public abstract class Menu {

    private static final ItemStack BORDER_ITEM = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    protected final ConfigurationSection config;
    /**
     * Menu items (slot -> button)
     */
    protected final NavigableMap<Integer, MenuButton> buttons = new TreeMap<>();
    protected final MenuService menuService;
    /**
     * Items that will be removed after a certain amount of time
     */
    private final Map<Integer, TemporaryButton> temporaryButtons = new HashMap<>();
    protected Inventory inventory;
    private Menu closeMenu;

    /**
     * Menu constructor
     *
     * @param path Path to menu in menu config
     * @param menuService Menu service
     */
    public Menu(String path, MenuService menuService) {
        config = menuService.getMenuConfig().getConfigurationSection(path);
        this.menuService = menuService;
    }

    /**
     * Get the inventory size for a given max number of items
     *
     * @param max Max number of items
     * @return Inventory size (in multiples of 9)
     */
    protected static int getInventorySize(int max) {
        if (max <= 0) return 9;
        int quotient = (int) Math.ceil(max / 9.0);
        return quotient > 5 ? 54 : quotient * 9;
    }

    /**
     * Check and remove expired temporary buttons
     *
     * @return Whether there are no more temporary buttons
     */
    protected boolean checkTemporaryButtons() {
        long now = System.currentTimeMillis();
        boolean changed = temporaryButtons.entrySet().removeIf(entry -> {
            if (entry.getValue().expiry() < now) {
                entry.getValue().future().complete(null);
                return true;
            }
            return false;
        });
        if (changed) {
            refresh();
        }
        return temporaryButtons.isEmpty();
    }

    /**
     * Refresh for all viewers
     */
    private void refresh() {
        for (HumanEntity viewer : inventory.getViewers()) {
            if (viewer instanceof Player) {
                refresh((Player) viewer);
            }
        }
    }

    /**
     * Add a temporary button to the menu
     *
     * @param player Player to add the button for
     * @param slot Slot to add the button to
     * @param path Path to button in menu config
     * @param expiry Expiry time
     * @param unit Expiry time unit
     * @return {@link CompletableFuture} that completes when the button expires
     */
    public CompletableFuture<Void> setTemporaryButton(Player player, int slot, String path, long expiry, TimeUnit unit) {
        return setTemporaryButton(slot, new ConfigButton(menuService.getMenuConfig().getConfigurationSection("Temporary-Buttons." + path), player, null), expiry, unit);
    }

    /**
     * Add a temporary button to the menu
     *
     * @param slot Slot to add the button to
     * @param newButton New button
     * @param expiry Expiry time
     * @param unit Expiry time unit
     * @return {@link CompletableFuture} that completes when the button expires
     */
    public CompletableFuture<Void> setTemporaryButton(int slot, MenuButton newButton, long expiry, TimeUnit unit) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        temporaryButtons.put(slot, new TemporaryButton(System.currentTimeMillis() + unit.toMillis(expiry), newButton, future));
        refresh();
        menuService.markForUpdate(this);
        return future;
    }

    /**
     * Fill empty space with {@link Menu#BORDER_ITEM}
     */
    private void fillBorder() {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                inventory.setItem(i, BORDER_ITEM);
            }
        }
    }

    /**
     * Open menu for player (see {@link MenuService#openMenu(Player, Menu) for practical opening}
     *
     * @param player Player to open menu for
     */
    protected void open(Player player) {
        refresh(player, true);
        player.openInventory(inventory);
    }

    /**
     * Called when the menu is closed
     *
     * @param player Player who closed the menu
     */
    public void onClosed(Player player) {
    }

    /**
     * Refresh menu for player
     *
     * @param player Target player
     */
    public void refresh(Player player) {
        refresh(player, false);
    }

    /**
     * Refresh menu for player
     *
     * @param player Target player
     * @param clearInventory Whether to clear the inventory (useful for when the size of {@link Menu#buttons} changes)
     */
    public void refresh(Player player, boolean clearInventory) {
        if (clearInventory) {
            buttons.clear();
            if (inventory != null)
                inventory.clear();
        }
        loadButtons(player);

        if (inventory == null) {
            inventory = Bukkit.createInventory(null, getSlots(), getTitle(player));
        }
        if (inventory.getSize() != getSlots()) {
            inventory = Bukkit.createInventory(null, getSlots(), getTitle(player));
            player.openInventory(inventory);
        }

        buttons.forEach((slot, button) -> inventory.setItem(slot, button.getItem()));
        temporaryButtons.forEach((slot, button) -> inventory.setItem(slot, button.tempButton().getItem()));
        if (clearInventory) {
            fillBorder();
        }
    }

    /**
     * Load menu buttons for a player.
     * By default, this will load all buttons in {@link Menu#config}
     *
     * @param player Target player
     */
    protected void loadButtons(Player player) {
        ConfigurationSection section = config.getConfigurationSection("items");
        if (section == null) {
            return;
        }
        Set<String> items = section.getKeys(false);
        String language = player == null ? null : player.locale().getLanguage();
        for (String item : items) {
            ConfigButton button = new ConfigButton(section.getConfigurationSection(item), language, getAction(item));
            editConfigButton(player, item, button);
            buttons.put(section.getInt(item + ".slot"), button);
        }
    }

    /**
     * Edit a button from the menu config.
     * Useful for adding dynamic values to buttons.
     *
     * @param player Player seeing the button
     * @param path Path to button in menu config
     * @param button Button instance to edit
     */
    protected void editConfigButton(Player player, String path, ConfigButton button) {

    }

    /**
     * Get the action for a button.
     * By default, this will return null.
     * Override this to add custom actions to config buttons.
     *
     * @param buttonPath Path to button in menu config
     * @return {@link ButtonAction} for the button
     */
    public ButtonAction getAction(String buttonPath) {
        return null;
    }

    /**
     * Get the title of the menu
     *
     * @param player Target player
     * @return Menu title
     */
    public String getTitle(Player player) {
        return LanguageService.color(QuestPlugin.getInstance().getLanguageService().get(config.getString("title"), player));
    }

    /**
     * Get the number of slots in the menu
     *
     * @return Number of slots
     */
    public int getSlots() {
        return Math.min(config.getInt("size", getSlots(buttons)), 54);
    }

    /**
     * Get the number of slots in the menu.
     * By default, this will return 9 if the menu is empty.
     * However, because {@link Menu#buttons} is a {@link NavigableMap}, it makes
     * it easier to get the last key (the last slot) and add 1 to it.
     *
     * @param buttons Buttons in the menu
     * @return Number of slots
     */
    protected int getSlots(NavigableMap<Integer, ?> buttons) {
        return buttons.isEmpty() ? 9 : getInventorySize(buttons.lastKey() + 1);
    }

    /**
     * Get the {@link MenuButton} in a specific slot
     *
     * @param slot Target slot
     * @return {@link MenuButton} in the slot
     */
    public MenuButton getButton(int slot) {
        return buttons.get(slot);
    }

    /**
     * Get the menu that will open when this menu is closed
     *
     * @return {@link Menu} to open
     */
    public Menu getCloseMenu() {
        return closeMenu;
    }

    /**
     * Set the menu that will open when this menu is closed
     *
     * @param closeMenu {@link Menu} to open
     */
    public void setCloseMenu(Menu closeMenu) {
        this.closeMenu = closeMenu;
    }

    private record TemporaryButton(long expiry, MenuButton tempButton, CompletableFuture<Void> future) {
    }
}
