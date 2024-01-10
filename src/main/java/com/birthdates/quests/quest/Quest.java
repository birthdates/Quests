package com.birthdates.quests.quest;

import com.birthdates.quests.QuestPlugin;
import com.birthdates.quests.util.LocaleUtil;
import com.birthdates.quests.util.format.Formattable;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public record Quest(String id, Material icon, String target, String permission, QuestType type,
                    BigDecimal requiredAmount, List<String> rewardCommands, String description, long expiry) {
    public Quest(String id, QuestType type) {
        this(id, Material.DIAMOND, null, null, type, BigDecimal.ONE, List.of(), "", -1);
    }

    public Quest icon(Material icon) {
        return new Quest(id, icon, target, permission, type, requiredAmount, rewardCommands, description, expiry);
    }

    public Quest description(String description) {
        return new Quest(id, icon, target, permission, type, requiredAmount, rewardCommands, description, expiry);
    }

    public Quest type(QuestType type) {
        return new Quest(id, icon, target, permission, type, requiredAmount, rewardCommands, description, expiry);
    }

    public Quest target(String target) {
        return new Quest(id, icon, target, permission, type, requiredAmount, rewardCommands, description, expiry);
    }

    public Quest permission(String permission) {
        return new Quest(id, icon, target, permission, type, requiredAmount, rewardCommands, description, expiry);
    }

    public Quest requiredAmount(BigDecimal requiredAmount) {
        return new Quest(id, icon, target, permission, type, requiredAmount, rewardCommands, description, expiry);
    }

    public Quest rewardCommands(List<String> rewardCommands) {
        return new Quest(id, icon, target, permission, type, requiredAmount, rewardCommands, description, expiry);
    }

    public Quest expiry(long expiry) {
        return new Quest(id, icon, target, permission, type, requiredAmount, rewardCommands, description, expiry);
    }

    /**
     * Formats the formattable with the quest information.
     *
     * @param player            The player to format the formattable for.
     * @param progress          The progress of the quest.
     * @param formattable       The formattable to format.
     * @param useProgressExpiry Whether to use the progress expiry or the quest expiry (static or dynamic).
     */
    public void formatButton(Player player, QuestProgress progress, Formattable formattable, boolean useProgressExpiry) {
        String any = QuestPlugin.getInstance().getLanguageService().get("messages.any", player);
        double percent = progress.amount().divide(requiredAmount, RoundingMode.HALF_EVEN).doubleValue() * 100D;
        formattable.setPlaceholder("%description%", description.split("\\\\n"))
                .setPlaceholder("%name%", LocaleUtil.formatID(type.name()))
                .setPlaceholder("%type%", LocaleUtil.formatID(type.name()))
                .setPlaceholder("%required%", LocaleUtil.formatNumber(requiredAmount))
                .setPlaceholder("%target%", target == null ? any : LocaleUtil.formatID(target))
                .setPlaceholder("%progress%", LocaleUtil.formatNumber(progress.amount()))
                .setPlaceholder("%expiry%", LocaleUtil.formatExpiry(player,
                        useProgressExpiry ? progress.expiry() - System.currentTimeMillis() : expiry))
                .setPlaceholder("%permission%", permission == null ? any : permission)
                .setPlaceholder("%rewards%", rewardCommands)
                .setPlaceholder("%icon%", LocaleUtil.formatID(icon.name()))
                .setPlaceholder("%progress_bar%", LocaleUtil.createProgressBar(percent));
    }
}
