package com.birthdates.quests.quest;

import org.bukkit.Material;

import java.math.BigDecimal;
import java.util.List;

public record Quest(String id, Material icon, String target, String permission, QuestType type,
                    BigDecimal requiredAmount, List<String> rewardCommands, String description) {

    public Quest(String id, QuestType type) {
        this(id, Material.DIAMOND, null, null, type, BigDecimal.ONE, List.of(), "");
    }

    public Quest icon(Material icon) {
        return new Quest(id, icon, target, permission, type, requiredAmount, rewardCommands, description);
    }

    public Quest description(String description) {
        return new Quest(id, icon, target, permission, type, requiredAmount, rewardCommands, description);
    }

    public Quest type(QuestType type) {
        return new Quest(id, icon, target, permission, type, requiredAmount, rewardCommands, description);
    }

    public Quest target(String target) {
        return new Quest(id, icon, target, permission, type, requiredAmount, rewardCommands, description);
    }

    public Quest permission(String permission) {
        return new Quest(id, icon, target, permission, type, requiredAmount, rewardCommands, description);
    }

    public Quest requiredAmount(BigDecimal requiredAmount) {
        return new Quest(id, icon, target, permission, type, requiredAmount, rewardCommands, description);
    }

    public Quest rewardCommands(List<String> rewardCommands) {
        return new Quest(id, icon, target, permission, type, requiredAmount, rewardCommands, description);
    }
}
