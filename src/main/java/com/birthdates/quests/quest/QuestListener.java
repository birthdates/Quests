package com.birthdates.quests.quest;

import com.birthdates.quests.data.QuestDataService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.math.BigDecimal;

public class QuestListener implements Listener {

    private final QuestDataService dataService;

    public QuestListener(QuestDataService dataService) {
        this.dataService = dataService;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        dataService.increaseProgress(event.getPlayer().getUniqueId(), QuestType.BREAK_BLOCKS, BigDecimal.ONE, event.getBlock().getType().name());
    }

    @EventHandler
    public void onDamageDealt(EntityDamageByEntityEvent event) {
        dataService.increaseProgress(event.getDamager().getUniqueId(), QuestType.DO_DAMAGE, BigDecimal.ONE, event.getEntity().getType().name());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }
        dataService.increaseProgress(killer.getUniqueId(), QuestType.KILL_ENTITY, BigDecimal.ONE, event.getEntity().getType().name());
    }
}
