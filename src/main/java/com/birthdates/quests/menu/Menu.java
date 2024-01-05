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

public abstract class Menu {

    private static final ItemStack BORDER_ITEM = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    protected final ConfigurationSection config;
    protected final NavigableMap<Integer, MenuButton> buttons = new TreeMap<>();
    protected final MenuService menuService;
    private final Map<Integer, TemporaryButton> temporaryButtons = new HashMap<>();
    protected Inventory inventory;
    private Menu closeMenu;

    public Menu(String path, MenuService menuService) {
        config = menuService.getMenuConfig().getConfigurationSection(path);
        this.menuService = menuService;
    }

    protected static int getInventorySize(int max) {
        if (max <= 0) return 9;
        int quotient = (int) Math.ceil(max / 9.0);
        return quotient > 5 ? 54 : quotient * 9;
    }

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

    private void refresh() {
        for (HumanEntity viewer : inventory.getViewers()) {
            if (viewer instanceof Player) {
                refresh((Player) viewer);
            }
        }
    }

    public CompletableFuture<Void> setTemporaryButton(Player player, int slot, String path, long expiry, TimeUnit unit) {
        return setTemporaryButton(slot, new ConfigButton(menuService.getMenuConfig().getConfigurationSection("Temporary-Buttons." + path), player, null), expiry, unit);
    }

    public CompletableFuture<Void> setTemporaryButton(int slot, MenuButton newButton, long expiry, TimeUnit unit) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        temporaryButtons.put(slot, new TemporaryButton(System.currentTimeMillis() + unit.toMillis(expiry), newButton, future));
        refresh();
        menuService.markForUpdate(this);
        return future;
    }

    private void fillBorder() {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                inventory.setItem(i, BORDER_ITEM);
            }
        }
    }

    protected void open(Player player) {
        refresh(player, true);
        player.openInventory(inventory);
    }

    public void onClosed(Player player) {
    }

    public void refresh(Player player) {
        refresh(player, false);
    }

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

    protected void editConfigButton(Player player, String path, ConfigButton button) {

    }

    public ButtonAction getAction(String buttonPath) {
        return null;
    }

    public String getTitle(Player player) {
        return LanguageService.color(QuestPlugin.getInstance().getLanguageService().get(config.getString("title"), player));
    }

    public int getSlots() {
        return Math.min(config.getInt("size", getRows(buttons)), 54);
    }

    public int getRows(NavigableMap<Integer, ?> buttons) {
        return buttons.isEmpty() ? 9 : getInventorySize(buttons.lastKey() + 1);
    }

    public MenuButton getButton(int slot) {
        return buttons.get(slot);
    }

    public Menu getCloseMenu() {
        return closeMenu;
    }

    public void setCloseMenu(Menu closeMenu) {
        this.closeMenu = closeMenu;
    }

    private record TemporaryButton(long expiry, MenuButton tempButton, CompletableFuture<Void> future) {
    }
}
