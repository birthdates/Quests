package com.birthdates.quests.menu;

import com.birthdates.quests.menu.button.ConfigButton;
import com.birthdates.quests.menu.button.MenuButton;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.NavigableMap;
import java.util.concurrent.TimeUnit;

/**
 * Abstract menu with pagination
 */
public abstract class PaginatedMenu extends Menu {
    /**
     * Slots used for the buttons
     */
    private final List<Integer> slots;
    private int page;

    public PaginatedMenu(String path, MenuService menuService) {
        super(path, menuService);
        slots = config.getIntegerList("slots");
    }

    @Override
    protected void loadButtons(Player player) {
        super.loadButtons(player);

        // Load buttons for current page
        int start = page * slots.size();
        int end = Math.min(start + slots.size(), getTotalButtons());
        for (int i = start; i < end; i++) {
            MenuButton button = getButton(player, i);
            if (button == null) {
                break;
            }
            int slot = slots.get(i - start);
            buttons.put(slot, button);
        }

        // Load next/previous buttons
        buttons.put(getNextSlot(), new ConfigButton(menuService.getMenuConfig().getConfigurationSection("Format.Next-Page"),
                player, (target, slot, clickType) -> goNextPage(target)));
        buttons.put(getBackSlot(), new ConfigButton(menuService.getMenuConfig().getConfigurationSection("Format.Previous-Page"),
                player, (target, slot, clickType) -> goBackPage(target)));
    }

    /**
     * Get the inventory size for a given set of buttons ignoring config slots
     *
     * @param buttons Buttons in the menu
     * @return Inventory size (in multiples of 9)
     */
    @Override
    public int getSlots(NavigableMap<Integer, ?> buttons) {
        if (slots.isEmpty()) {
            return 9;
        }
        return getInventorySize(slots.get(slots.size() - 1));
    }

    /**
     * Get the slot for the next page button
     *
     * @return Next page slot
     */
    protected int getNextSlot() {
        return getSlots() - 4;
    }

    /**
     * Get the slot for the previous page button
     *
     * @return Previous page slot
     */
    protected int getBackSlot() {
        return getSlots() - 6;
    }

    /**
     * Get the button for a specific index
     *
     * @param player Target player
     * @param index  Index of the button (not a slot but index in a list of data, see {@link PaginatedMenu#getTotalButtons()})
     * @return {@link MenuButton} for the index
     */
    protected abstract MenuButton getButton(Player player, int index);

    public int getCurrentPage() {
        return page;
    }

    public void goBackPage(Player player) {
        if (page < 1) {
            setTemporaryButton(player, getBackSlot(), "No-Previous-Page", 3, TimeUnit.SECONDS);
            return;
        }
        page--;
        refresh(player, true);
    }

    public void goNextPage(Player player) {
        if ((page + 1) >= getMaxPage()) {
            setTemporaryButton(player, getNextSlot(), "No-Next-Page", 3, TimeUnit.SECONDS);
            return;
        }
        page++;
        refresh(player, true);
    }

    public int getMaxPage() {
        return (int) Math.ceil(getTotalButtons() / (double) slots.size());
    }

    /**
     * Get the total number of buttons in the menu (on every page)
     *
     * @return Total number of buttons
     */
    public abstract int getTotalButtons();
}
