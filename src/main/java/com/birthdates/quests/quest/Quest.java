package com.birthdates.quests.quest;

import org.bukkit.Material;

import java.math.BigDecimal;
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
}
