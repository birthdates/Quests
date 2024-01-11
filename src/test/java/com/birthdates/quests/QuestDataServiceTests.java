package com.birthdates.quests;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.birthdates.quests.quest.QuestProgress;
import com.birthdates.quests.quest.QuestStatus;
import com.birthdates.quests.quest.QuestType;
import com.birthdates.quests.util.LocaleUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class QuestDataServiceTests {

    private ServerMock server;
    private QuestPlugin questPlugin;
    private PlayerMock player;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        questPlugin = MockBukkit.load(QuestPlugin.class);
        player = server.addPlayer();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    private void createTestQuest() {
        createTestQuest(QuestType.values()[0], "perm");
    }

    private void createTestQuest(QuestType type, String expiry) {
        PlayerMock player = server.addPlayer();
        player.setOp(true);

        // Create quest
        player.performCommand("quest admin");
        player.simulateInventoryClick(4);
        player.chat("test");
        TestUtil.waitTick();
        player.chat(type.name());
        TestUtil.waitTick();

        // Set expiry
        player.performCommand("quest admin");
        player.simulateInventoryClick(10);
        player.simulateInventoryClick(40);
        player.chat(expiry);
        TestUtil.waitTick();
    }

    @Test
    public void testActivation() {
        createTestQuest();
        player.performCommand("quest");
        player.simulateInventoryClick(10);
        assertEquals(QuestStatus.IN_PROGRESS,
                questPlugin.getDataService().getProgress(player.getUniqueId(), "test").status());
    }

    @Test
    public void testCancellation() {
        testActivation();
        player.performCommand("quest");
        player.simulateInventoryClick(10);
        assertEquals(QuestStatus.NOT_STARTED,
                questPlugin.getDataService().getProgress(player.getUniqueId(), "test").status());
    }

    @Test
    public void testProgress() {
        WorldMock worldMock = server.addSimpleWorld("world");
        for (QuestType value : QuestType.values()) {
            testActivation();
            createTestQuest(value, "perm");
            switch (value) {
                case BREAK_BLOCKS -> player.simulateBlockBreak(worldMock.getBlockAt(0, 0, 0));

                // Unsupported in MockBukkit:
                case DO_DAMAGE, KILL_ENTITY -> {
                    return;
                }
            }

            questPlugin.getDataService().saveAllProgress();
            server.getScheduler().waitAsyncTasksFinished();
            assertEquals(QuestStatus.COMPLETED,
                    questPlugin.getDataService().getProgress(player.getUniqueId(), "test").status());
            questPlugin.getDataService().invalidateQuestData(player.getUniqueId());
        }
    }

    @Test
    public void testAlerts() {
        testActivation();
        server.addPlayer(player);
        server.getScheduler().waitAsyncTasksFinished();
        String message = player.nextMessage();
        String expected = questPlugin.getLanguageService().get("messages.quest.active-quests", player.locale().getLanguage());
        assertEquals(LocaleUtil.color(expected), message);
    }

    @Test
    public void testExpiredQuest() {
        createTestQuest(QuestType.values()[0], "1s");

        // Activate quest
        player.performCommand("quest");
        player.simulateInventoryClick(10);

        // Check quest is active
        QuestProgress progress = questPlugin.getDataService().getProgress(player.getUniqueId(), "test");
        assertEquals(QuestStatus.IN_PROGRESS, progress.status());

        // Wait for quest to expire
        TestUtil.waitFor(1000L);

        // Check quest is expired
        progress = questPlugin.getDataService().getProgress(player.getUniqueId(), "test");
        assertFalse(progress.isInProgress());
    }
}
