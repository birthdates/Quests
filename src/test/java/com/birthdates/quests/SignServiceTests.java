package com.birthdates.quests;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SignServiceTests {
    private QuestPlugin questPlugin;
    private PlayerMock player;

    @BeforeEach
    public void setUp() {
        ServerMock server = MockBukkit.mock();
        questPlugin = MockBukkit.load(QuestPlugin.class);
        player = server.addPlayer();
        player.setOp(true);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    private Block createTestSign() {
        Block blockMock = player.getTargetBlockExact(100);
        assertNotNull(blockMock);
        blockMock.setType(Material.ACACIA_SIGN);
        return blockMock;
    }

    @Test
    public void testSignCreation() {
        createTestSign();
        player.performCommand("quest sign 1");
        TestUtil.waitTick();
        assertFalse(questPlugin.getSignService().getAllSignLocations().isEmpty());
    }

    @Test
    public void testSignDeletion() {
        player.simulateBlockBreak(createTestSign());
        assertTrue(questPlugin.getSignService().getAllSignLocations().isEmpty());
    }
}
