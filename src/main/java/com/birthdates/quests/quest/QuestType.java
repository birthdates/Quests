package com.birthdates.quests.quest;

import com.birthdates.quests.QuestPlugin;
import com.birthdates.quests.util.LocaleUtil;
import org.bukkit.entity.Player;

/**
 * The action a quest requires
 */
public enum QuestType {
    BREAK_BLOCKS,
    DO_DAMAGE,
    KILL_ENTITY;

    /**
     * Show all available quest types to a player
     *
     * @param player Target player
     */
    public static void showQuestTypes(Player player) {
        var languageService = QuestPlugin.getInstance().getLanguageService();
        player.sendMessage(LocaleUtil.color(languageService.get("messages.quest.create-admin-type", player)));
        for (QuestType type : QuestType.values()) {
            player.sendMessage(LocaleUtil.color("    &a* &f" + type.name()));
        }
    }
}
