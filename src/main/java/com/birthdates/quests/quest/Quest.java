package com.birthdates.quests.quest;

import org.bukkit.Material;

import java.math.BigDecimal;
import java.util.List;

public record Quest(String id, Material icon, String target, String permission, QuestType type,
                    BigDecimal requiredAmount, List<String> rewardCommands, String description) {

    public Quest(String id, QuestType type) {
        this(id, Material.DIAMOND, null, null, type, BigDecimal.ONE, List.of(), "");
    }

}
