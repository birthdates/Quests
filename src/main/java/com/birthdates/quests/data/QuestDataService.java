package com.birthdates.quests.data;

import com.birthdates.quests.config.QuestConfig;
import com.birthdates.quests.quest.Quest;
import com.birthdates.quests.quest.QuestProgress;
import com.birthdates.quests.quest.QuestStatus;
import com.birthdates.quests.quest.QuestType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public abstract class QuestDataService implements Listener {

    protected final Map<UUID, Map<String, QuestProgress>> userQuestProgress = new ConcurrentHashMap<>();
    protected final QuestConfig questConfig;
    private final Map<UUID, Map<String, BigDecimal>> progressUpdates = new ConcurrentHashMap<>();
    private final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    private final int maxActiveQuests;

    public QuestDataService(QuestConfig questConfig, int maxActiveQuests) {
        service.scheduleAtFixedRate(this::saveProgress, 0, 5, java.util.concurrent.TimeUnit.SECONDS);
        this.questConfig = questConfig;
        this.maxActiveQuests = maxActiveQuests;
    }

    public void unload() {
        saveProgress();
        service.shutdown();
    }

    public String activateQuest(Player player, Quest quest) {
        if (quest.permission() != null && !player.hasPermission(quest.permission())) {
            return "no_quest_permission";
        }
        Map<String, QuestProgress> activeQuests = userQuestProgress.computeIfAbsent(player.getUniqueId(), uuid -> new ConcurrentHashMap<>());
        if (activeQuests.size() >= maxActiveQuests) {
            return "max_quests_active";
        }
        activeQuests.put(quest.id(), new QuestProgress(BigDecimal.ZERO, QuestStatus.IN_PROGRESS));
        increaseProgress(player.getUniqueId(), quest.type(), BigDecimal.ZERO, quest.target()); // Save to database
        return null;
    }

    private void saveProgress() {
        progressUpdates.forEach(this::saveProgress);
        progressUpdates.clear();
    }

    private void saveProgress(UUID userID, Map<String, BigDecimal> questProgress) {
        for (Map.Entry<String, BigDecimal> entry : questProgress.entrySet()) {
            saveProgress(userID, entry.getKey(), entry.getValue());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        loadPlayerData(event.getPlayer().getUniqueId());
    }

    public void incrementProgress(UUID playerId, QuestType type, String target) {
        increaseProgress(playerId, type, BigDecimal.ONE, target);
    }

    public void increaseProgress(UUID playerId, QuestType type, BigDecimal amount, String target) {
        Map<String, QuestProgress> activeQuests = userQuestProgress.get(playerId);
        if (activeQuests == null) return;
        for (String questId : activeQuests.keySet()) {
            Quest quest = questConfig.getQuest(questId);
            if (quest.type() != type || quest.target() != null && quest.target().equals(target)) {
                continue;
            }
            QuestProgress progress = activeQuests.get(questId);
            if (progress.status() == QuestStatus.IN_PROGRESS) {
                increaseProgress(playerId, questId, amount);
            }
        }
    }

    public void increaseProgress(UUID playerId, String questId, BigDecimal amount) {
        progressUpdates.computeIfAbsent(playerId, uuid -> new ConcurrentHashMap<>())
                .compute(questId, (s, bigDecimal) -> bigDecimal == null ? amount : bigDecimal.add(amount));
    }

    public abstract void loadPlayerData(UUID playerId);

    public QuestProgress getProgress(UUID playerId, String questId) {
        return userQuestProgress.computeIfAbsent(playerId, uuid -> new ConcurrentHashMap<>())
                .computeIfAbsent(questId, s -> new QuestProgress(BigDecimal.ZERO, QuestStatus.IN_PROGRESS));
    }

    protected abstract void saveProgress(UUID playerId, String questId, BigDecimal amount);
}
