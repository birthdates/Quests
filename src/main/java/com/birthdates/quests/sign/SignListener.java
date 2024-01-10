package com.birthdates.quests.sign;

import com.birthdates.quests.QuestPlugin;
import com.birthdates.quests.data.QuestDataService;
import com.birthdates.quests.event.QuestCancelEvent;
import com.birthdates.quests.event.QuestDataLoadedEvent;
import com.birthdates.quests.event.QuestProgressEvent;
import com.birthdates.quests.lang.LanguageService;
import com.birthdates.quests.quest.Quest;
import com.birthdates.quests.quest.QuestProgress;
import com.birthdates.quests.util.LocaleUtil;
import com.birthdates.quests.util.format.FormattableString;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

/**
 * Listener for sign events (updating signs, removing signs, etc)
 */
public class SignListener implements Listener {
    private final SignService signService;
    private final LanguageService languageService;
    private final QuestDataService dataService;

    public SignListener(SignService signService, LanguageService languageService, QuestDataService dataService) {
        this.signService = signService;
        this.languageService = languageService;
        this.dataService = dataService;
    }

    /**
     * Force update all quest signs for a player
     *
     * @param player The player to update signs for
     */
    public void forceUpdate(Player player) {
        signService.getAllSignLocations()
                .stream()
                .filter(x -> x.location().getWorld().equals(player.getWorld()))
                .forEach(sign -> forceUpdate(sign, player));
    }

    /**
     * Force update a sign for all players
     *
     * @param sign The sign to update
     */
    public void forceUpdate(QuestSign sign) {
        Bukkit.getOnlinePlayers().forEach(player -> forceUpdate(sign, player));
    }

    /**
     * Force update a sign for a player
     *
     * @param sign   The sign to update
     * @param player The player to update the sign for
     */
    public void forceUpdate(QuestSign sign, Player player) {
        List<Quest> quests = dataService.getActiveQuests(player);
        Quest quest = quests.size() > sign.questNumber() ? quests.get(sign.questNumber()) : null;
        sendSignLines(player, quest, sign);
    }

    /**
     * Send sign lines for a quest to a player
     *
     * @param player The player to send the sign lines to
     * @param quest  The quest to send the sign lines for
     * @param sign   The sign to send the sign lines for
     */
    private void sendSignLines(Player player, Quest quest, QuestSign sign) {
        String lang = quest != null ? "signs.quest-format" : "signs.no-quest-active";
        FormattableString str = new FormattableString(languageService.get(lang, player));
        if (quest != null) {
            QuestProgress progress = dataService.getProgress(player.getUniqueId(), quest.id());
            quest.formatButton(player, progress, str, true);
        }
        String[] formatLines = str.getLines();
        String[] lines = new String[4];
        for (int i = 0; i < lines.length; i++) {
            lines[i] = LocaleUtil.color(formatLines.length > i ? formatLines[i] : "");
        }
        player.sendSignChange(sign.location(), lines);
    }

    @EventHandler
    public void onQuestProgress(QuestProgressEvent event) {
        Bukkit.getScheduler().runTask(QuestPlugin.getInstance(), () -> {
            Player player = Bukkit.getPlayer(event.getPlayerId());
            if (player != null) {
                forceUpdate(player); // Force update all because order can change
            }
        });
    }

    @EventHandler
    public void onSignInteract(PlayerInteractEvent event) {
        if (!event.hasBlock() || !(event.getClickedBlock().getState() instanceof Sign) || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        QuestSign sign = signService.getSign(event.getClickedBlock().getLocation());
        if (sign != null) {
            event.setCancelled(true);
            forceUpdate(sign, event.getPlayer()); // This overwrites the block update that would normally happen
        }
    }

    @EventHandler
    public void onQuestCancel(QuestCancelEvent event) {
        Player player = Bukkit.getPlayer(event.getPlayerId());
        if (player != null) {
            forceUpdate(player);
        }
    }

    @EventHandler
    public void onSignEdit(SignChangeEvent event) {
        if (signService.isSign(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (signService.isSign(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!(event.getBlock().getState() instanceof Sign)) {
            return;
        }
        if (!event.getPlayer().hasPermission("quests.admin")) {
            if (signService.isSign(event.getBlock().getLocation())) {
                event.setCancelled(true);
            }
            return;
        }
        if (signService.removeSignLocation(event.getBlock().getLocation())) {
            languageService.display(event.getPlayer(), "messages.admin.sign-removed");
        }
    }

    @EventHandler
    public void onJoin(QuestDataLoadedEvent event) {
        // Delay to allow chunks to send to client
        Bukkit.getScheduler().runTaskLater(QuestPlugin.getInstance(), () ->
                forceUpdate(event.getPlayer()), 20L);
    }

    @EventHandler
    public void onSwitchWorld(PlayerChangedWorldEvent event) {
        forceUpdate(event.getPlayer());
    }
}
