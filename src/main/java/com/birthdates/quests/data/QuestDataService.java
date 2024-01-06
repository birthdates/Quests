package com.birthdates.quests.data;

import com.birthdates.quests.QuestPlugin;
import com.birthdates.quests.config.QuestConfig;
import com.birthdates.quests.lang.LanguageService;
import com.birthdates.quests.quest.Quest;
import com.birthdates.quests.quest.QuestProgress;
import com.birthdates.quests.quest.QuestStatus;
import com.birthdates.quests.quest.QuestType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

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
        service.scheduleAtFixedRate(this::checkExpiredQuests, 0, 1, TimeUnit.SECONDS);
    }

    public void unload() {
        saveProgress();
        service.shutdown();
    }

    private void checkExpiredQuests() {
        userQuestProgress.forEach((id, data) -> data.entrySet().removeIf(entry -> {
            if (!entry.getValue().isExpired()) {
                return false;
            }
            deleteProgress(id, entry.getKey());
            Player player = Bukkit.getPlayer(id);
            if (player != null) {
                QuestPlugin.getInstance().getLanguageService().display(player, "messages.quest.expired");
            }
            return true;
        }));
    }

    public String activateQuest(Player player, Quest quest) {
        if (quest.permission() != null && !player.hasPermission(quest.permission())) {
            return "No_Quest_Permission";
        }
        Map<String, QuestProgress> activeQuests = userQuestProgress.computeIfAbsent(player.getUniqueId(), uuid -> new ConcurrentHashMap<>());
        if (activeQuests.size() >= maxActiveQuests) {
            return "Max_Quests_Active";
        }
        long expiry = quest.expiry() < 0 ? -1 : System.currentTimeMillis() + quest.expiry();
        activeQuests.put(quest.id(), new QuestProgress(BigDecimal.ZERO, QuestStatus.IN_PROGRESS, expiry));
        increaseProgress(player.getUniqueId(), quest.id(), BigDecimal.ZERO); // Save to database
        return null;
    }

    public String cancelQuest(Player player, Quest quest) {
        Map<String, QuestProgress> activeQuests = userQuestProgress.get(player.getUniqueId());
        if (activeQuests == null) return "Quest_Not_Active";
        QuestProgress progress = activeQuests.get(quest.id());
        if (progress == null) return "Quest_Not_Active";
        if (progress.status() == QuestStatus.COMPLETED) return "Quest_Already_Completed";
        activeQuests.remove(quest.id());
        if (activeQuests.isEmpty()) {
            userQuestProgress.remove(player.getUniqueId());
        }
        deleteProgress(player.getUniqueId(), quest.id());
        return null;
    }

    private void saveProgress() {
        progressUpdates.forEach(this::saveProgress);
        progressUpdates.clear();
    }

    private void onQuestFinished(UUID playerId, Quest quest) {
        Player player = Bukkit.getPlayer(playerId);
        if (player == null) {
            return;
        }
        for (String rewardCommand : quest.rewardCommands()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    rewardCommand.replace("%name%", player.getName()).replace("%id%", playerId.toString()));
        }

        QuestPlugin.getInstance().getLanguageService().display(player, "messages.quest.complete");
    }

    private void saveProgress(UUID userID, Map<String, BigDecimal> questProgress) {
        var progressMap = userQuestProgress.computeIfAbsent(userID, uuid -> new ConcurrentHashMap<>());
        for (Map.Entry<String, BigDecimal> entry : questProgress.entrySet()) {
            Quest quest = questConfig.getQuest(entry.getKey());
            QuestProgress progress = progressMap.computeIfAbsent(entry.getKey(), s -> QuestProgress.NOT_STARTED).add(entry.getValue());
            if (!progress.isExpired() && progress.status() == QuestStatus.IN_PROGRESS && progress.amount().compareTo(quest.requiredAmount()) >= 0) {
                progress = progress.status(QuestStatus.COMPLETED);
                Bukkit.getScheduler().runTask(QuestPlugin.getInstance(), () -> onQuestFinished(userID, quest));
            }
            saveProgress(userID, entry.getKey(), progress);
            progressMap.put(entry.getKey(), progress);
        }
    }

    private void alertActiveQuests(Player player) {
        var activeQuests = userQuestProgress.get(player.getUniqueId());
        if (activeQuests == null) return;

        var languageService = QuestPlugin.getInstance().getLanguageService();
        languageService.display(player, "messages.quest.active-quests");
        activeQuests.forEach((questId, progress) -> {
            Quest quest = questConfig.getQuest(questId);
            if (quest == null) return;
            double percent = progress.amount().divide(quest.requiredAmount(), RoundingMode.HALF_EVEN).doubleValue() * 100.0D;
            languageService.display(player, "messages.quest.active-quest",
                    quest.description(), LanguageService.formatID(quest.type().name()), LanguageService.formatNumber(progress.amount()),
                    LanguageService.formatNumber(quest.requiredAmount()), LanguageService.createProgressBar(percent));
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        loadPlayerData(event.getPlayer().getUniqueId()).thenRun(() -> alertActiveQuests(event.getPlayer()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        userQuestProgress.remove(event.getPlayer().getUniqueId());
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

    public abstract CompletableFuture<Void> loadPlayerData(UUID playerId);

    public abstract void deleteProgress(UUID playerId, String questId);

    public QuestProgress getProgress(UUID playerId, String questId) {
        return userQuestProgress.computeIfAbsent(playerId, uuid -> new ConcurrentHashMap<>())
                .getOrDefault(questId, QuestProgress.NOT_STARTED);
    }

    protected abstract void saveProgress(UUID playerId, String questId, QuestProgress progress);
}
