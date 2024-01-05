package com.birthdates.quests.menu;

import com.birthdates.quests.QuestPlugin;
import com.birthdates.quests.lang.LanguageService;
import com.birthdates.quests.menu.button.ButtonAction;
import com.birthdates.quests.menu.button.ConfigButton;
import com.birthdates.quests.menu.button.MenuButton;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

public abstract class Menu {

    private static final ItemStack BORDER_ITEM = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    protected final ConfigurationSection config;
    protected final NavigableMap<Integer, MenuButton> buttons = new TreeMap<>();
    protected Inventory inventory;
    private Menu closeMenu;

    public Menu(String path, YamlConfiguration menuConfig) {
        config = menuConfig.getConfigurationSection(path);
    }

    public Menu(String path, MenuService menuService) {
        this(path, menuService.getMenuConfig());
    }

    protected static int getInventorySize(int max) {
        if (max <= 0) return 9;
        int quotient = (int) Math.ceil(max / 9.0);
        return quotient > 5 ? 54 : quotient * 9;
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
            MenuButton button = new ConfigButton(section.getConfigurationSection(item), language, getAction(item));
            buttons.put(section.getInt(item + ".slot"), button);
        }
    }

    public ButtonAction getAction(String buttonPath) {
        return null;
    }

    public String getTitle(Player player) {
        return LanguageService.color(QuestPlugin.getInstance().getLanguageService().get(config.getString("title"), player));
    }

    public int getSlots() {
        return Math.min(config.getInt("rows", getRows(buttons)), 54);
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
}
