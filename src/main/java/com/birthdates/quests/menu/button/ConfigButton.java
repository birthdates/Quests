package com.birthdates.quests.menu.button;

import com.birthdates.quests.QuestPlugin;
import com.birthdates.quests.util.LocaleUtil;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ConfigButton implements MenuButton {

    protected final String language;
    private final ItemStack itemStack;
    private final ButtonAction action;
    private final Sound sound;

    public ConfigButton(ConfigurationSection section, Player player, ButtonAction action) {
        this(section, player == null ? null : player.locale().getLanguage(), action);
    }

    public ConfigButton(ConfigurationSection section, String language, ButtonAction action) {
        itemStack = deserialize(section);
        this.action = action;
        this.language = language;
        if (language != null) {
            loadLanguage();
        }
        sound = section.contains("sound") ? Sound.valueOf(section.getString("sound")) : null;
    }

    private static ItemStack deserialize(ConfigurationSection section) {
        var itemStack = new ItemStack(Material.valueOf(section.getString("item")));
        var meta = itemStack.getItemMeta();
        if (section.contains("name")) {
            meta.setDisplayName(section.getString("name"));
        }
        if (section.contains("lore")) {
            meta.setLore(section.getStringList("lore"));
        }
        if (section.contains("color")) {
            String hex = section.getString("color").replace("#", "");
            Color color = Color.fromRGB(Integer.parseInt(hex, 16));
            if (meta instanceof LeatherArmorMeta) {
                ((LeatherArmorMeta) meta).setColor(color);
            }
            if (meta instanceof PotionMeta) {
                ((org.bukkit.inventory.meta.PotionMeta) meta).setColor(color);
            }
        }
        if (section.getBoolean("glow")) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, false);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private void loadLanguage() {
        var meta = itemStack.getItemMeta();
        if (meta.hasDisplayName()) {
            String name = QuestPlugin.getInstance().getLanguageService().get(meta.getDisplayName(), language);
            meta.setDisplayName(LocaleUtil.color(name));
        }
        if (meta.hasLore()) {
            var lore = meta.getLore();
            List<String> newLore = new ArrayList<>();
            for (String entry : lore) {
                List<String> toAdd = new ArrayList<>(QuestPlugin.getInstance().getLanguageService().getList(entry, language));
                toAdd.replaceAll(LocaleUtil::color);
                newLore.addAll(toAdd);
            }
            meta.setLore(newLore);
        }
        itemStack.setItemMeta(meta);
    }

    public ConfigButton setPlaceholder(String placeholder, String value) {
        var meta = itemStack.getItemMeta();
        if (meta.hasDisplayName()) {
            meta.setDisplayName(meta.getDisplayName().replace(placeholder, value));
        }
        if (meta.hasLore()) {
            var lore = meta.getLore();
            lore.replaceAll(s -> s.replace(placeholder, value));
            meta.setLore(lore);
        }
        itemStack.setItemMeta(meta);
        return this;
    }

    public ConfigButton setPlaceholder(String placeholder, Collection<String> value) {
        return setPlaceholder(placeholder, value.toArray(new String[0]));
    }

    public ConfigButton setPlaceholder(String placeholder, String[] value) {
        var meta = itemStack.getItemMeta();
        if (meta.hasLore()) {
            var lore = meta.getLore();
            var newLore = new ArrayList<String>();
            for (String str : lore) {
                if (!str.contains(placeholder)) {
                    newLore.add(str);
                    continue;
                }
                newLore.addAll(Arrays.asList(value));
            }
            meta.setLore(newLore);
        }
        itemStack.setItemMeta(meta);
        return this;
    }

    @Override
    public ItemStack getItem() {
        return itemStack;
    }

    @Override
    public void onClick(Player player, int slot, ClickType clickType) {
        if (action != null) {
            action.click(player, slot, clickType);
        }
        if (sound != null) {
            player.playSound(player.getLocation(), sound, 0.75f, 1);
        }
    }
}
