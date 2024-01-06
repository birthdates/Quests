package com.birthdates.quests;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.WorldMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.birthdates.quests.quest.QuestStatus;
import com.birthdates.quests.quest.QuestType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        createTestQuest(QuestType.values()[0]);
    }

    private void createTestQuest(QuestType type) {
        PlayerMock player = server.addPlayer();
        player.setOp(true);
        player.performCommand("quest admin");
        player.simulateInventoryClick(4);
        player.chat("test");
        TestUtil.waitTick();
        player.chat(type.name());
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
            createTestQuest(value);
            switch (value) {
                case BREAK_BLOCKS -> player.simulateBlockBreak(worldMock.getBlockAt(0, 0, 0));

                // Unsupported:
                case DO_DAMAGE -> {
                    return;
                }
                case KILL_ENTITY -> {
                    return;
                }
            }

            questPlugin.getDataService().saveProgress();
            TestUtil.waitTick();
            assertEquals(QuestStatus.COMPLETED,
                    questPlugin.getDataService().getProgress(player.getUniqueId(), "test").status());
            questPlugin.getDataService().invalidateQuestData(player.getUniqueId());
        }
    }
}
