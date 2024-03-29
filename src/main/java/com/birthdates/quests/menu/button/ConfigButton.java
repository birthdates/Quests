package com.birthdates.quests.menu.button;

import com.birthdates.quests.QuestPlugin;
import com.birthdates.quests.util.EnumUtil;
import com.birthdates.quests.util.LocaleUtil;
import com.birthdates.quests.util.format.Formattable;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ConfigButton implements MenuButton, Formattable {
    protected final String language;
    private final ItemStack itemStack;
    private final ButtonAction action;
    private Sound sound;

    public ConfigButton(ConfigurationSection section, Player player, ButtonAction action) {
        this(section, player == null ? null : player.locale().getLanguage(), action);
    }

    public ConfigButton(ConfigurationSection section, String language, ButtonAction action) {
        itemStack = deserialize(section);
        this.action = action;
        this.language = language;
        if (language != null) {
            formatLanguageKeys();
        }

        if (section.contains("sound")) {
            String soundStr = section.getString("sound");
            if (EnumUtil.isInvalidEnum(Sound.class, soundStr)) {
                QuestPlugin.getInstance().getLogger().warning("Invalid sound: " + soundStr);
                return;
            }
            sound = Sound.valueOf(section.getString("sound"));
        }
    }

    /**
     * Deserialize an {@link ItemStack} from {@link ConfigurationSection}
     *
     * @param section The section to deserialize from
     * @return The deserialized {@link ItemStack}
     */
    private static ItemStack deserialize(ConfigurationSection section) {
        String material = section.getString("item");
        if (EnumUtil.isInvalidEnum(Material.class, material)) {
            QuestPlugin.getInstance().getLogger().warning("Invalid material: " + material);
            return new ItemStack(Material.BARRIER);
        }

        var itemStack = new ItemStack(Material.valueOf(material));
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
                ((PotionMeta) meta).setColor(color);
                meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
            }
        }

        if (section.getBoolean("glow")) {
            meta.addEnchant(org.bukkit.enchantments.Enchantment.DURABILITY, 1, false);
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
        }

        if (section.contains("model")) {
            meta.setCustomModelData(section.getInt("model"));
        }

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    /**
     * Format all language keys in the {@link ItemStack} (lore, name)
     */
    private void formatLanguageKeys() {
        var meta = itemStack.getItemMeta();
        if (meta.hasDisplayName()) {
            String name = QuestPlugin.getInstance().getLanguageService().get(meta.getDisplayName(), language);
            meta.setDisplayName(LocaleUtil.color(name));
        }
        if (meta.hasLore()) {
            // Support for multiple lines
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

    /**
     * Replace a placeholder in the {@link ItemStack} (lore, name)
     *
     * @param placeholder The placeholder to replace
     * @param value       The value to replace the placeholder with
     * @return The {@link ConfigButton} instance (for chaining)
     */
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

    /**
     * Replace a placeholder in the {@link ItemStack} (lore, name)
     *
     * @param placeholder The placeholder to replace
     * @param value       The value to replace the placeholder with
     * @return The {@link ConfigButton} instance (for chaining)
     */
    public ConfigButton setPlaceholder(String placeholder, Collection<String> value) {
        return setPlaceholder(placeholder, value.toArray(new String[0]));
    }

    @Override
    public String[] getLines() {
        throw new UnsupportedOperationException();
    }

    /**
     * Replace a placeholder in the {@link ItemStack} (only lore)
     *
     * @param placeholder The placeholder to replace
     * @param values      The values to replace the placeholder with (will be added to the lore)
     * @return The {@link ConfigButton} instance (for chaining)
     */
    public ConfigButton setPlaceholder(String placeholder, String[] values) {
        var meta = itemStack.getItemMeta();
        if (meta.hasLore()) {
            var lore = meta.getLore();
            var newLore = new ArrayList<String>();
            for (String str : lore) {
                if (!str.contains(placeholder)) {
                    newLore.add(str);
                    continue;
                }
                newLore.addAll(Arrays.asList(values));
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
            action.onClick(player, slot, clickType);
        }
        if (sound != null) {
            player.playSound(player.getLocation(), sound, 0.75f, 1);
        }
    }
}
