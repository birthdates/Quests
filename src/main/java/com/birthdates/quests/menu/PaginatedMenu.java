package com.birthdates.quests.menu;

import com.birthdates.quests.menu.button.ConfigButton;
import com.birthdates.quests.menu.button.MenuButton;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.NavigableMap;

public abstract class PaginatedMenu extends Menu {
    private final List<Integer> slots = config.getIntegerList("slots");
    private final MenuService menuService;
    private int page;

    public PaginatedMenu(String path, MenuService menuService) {
        super(path, menuService);
        this.menuService = menuService;
    }

    @Override
    protected void loadButtons(Player player) {
        super.loadButtons(player);

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

        buttons.put(getNextSlot(), new ConfigButton(menuService.getMenuConfig().getConfigurationSection("Format.Next-Page"),
                player, (target, slot, clickType) -> goNextPage(target)));
        buttons.put(getBackSlot(), new ConfigButton(menuService.getMenuConfig().getConfigurationSection("Format.Previous-Page"),
                player, (target, slot, clickType) -> goBackPage(target)));
    }

    @Override
    public int getRows(NavigableMap<Integer, ?> buttons) {
        return getInventorySize(slots.get(slots.size() - 1));
    }

    protected int getNextSlot() {
        return getSlots() - 4;
    }

    protected int getBackSlot() {
        return getSlots() - 6;
    }

    protected abstract MenuButton getButton(Player player, int index);

    public int getPage() {
        return page;
    }

    public void goBackPage(Player player) {
        if (page < 1) {
            return;
        }
        page--;
        refresh(player, true);
    }

    public void goNextPage(Player player) {
        if ((page + 1) >= getMaxPage()) {
            return;
        }
        page++;
        refresh(player, true);
    }

    public int getMaxPage() {
        return (int) Math.ceil(getTotalButtons() / (double) slots.size());
    }

    public abstract int getTotalButtons();
}
