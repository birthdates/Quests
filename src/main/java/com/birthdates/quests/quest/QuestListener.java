package com.birthdates.quests.quest;

import com.birthdates.quests.data.QuestDataService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.math.BigDecimal;

/**
 * Listeners for specific {@link QuestType} events
 */
public class QuestListener implements Listener {

    private final QuestDataService dataService;

    public QuestListener(QuestDataService dataService) {
        this.dataService = dataService;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        dataService.incrementProgress(event.getPlayer().getUniqueId(), QuestType.BREAK_BLOCKS, event.getBlock().getType().name());
    }

    @EventHandler
    public void onDamageDealt(EntityDamageByEntityEvent event) {
        dataService.increaseProgress(event.getDamager().getUniqueId(), QuestType.DO_DAMAGE, BigDecimal.valueOf(event.getDamage()), event.getEntity().getType().name());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        dataService.incrementProgress(killer.getUniqueId(), QuestType.KILL_ENTITY, event.getEntity().getType().name());
    }
}
