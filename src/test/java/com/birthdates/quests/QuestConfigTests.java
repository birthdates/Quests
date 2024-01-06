package com.birthdates.quests;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.birthdates.quests.lang.LanguageService;
import com.birthdates.quests.quest.QuestType;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.Testable;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class QuestConfigTests {

    private ServerMock server;
    private QuestPlugin questPlugin;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
        questPlugin = MockBukkit.load(QuestPlugin.class);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testCreateQuest() {
        PlayerMock player = server.addPlayer();
        player.setOp(true);
        player.performCommand("quest admin");
        player.simulateInventoryClick(4);
        player.chat("test");
        TestUtil.waitTick();
        player.chat(QuestType.values()[0].name());
        TestUtil.waitTick();
        assertFalse(questPlugin.getQuestConfig().getAllQuests().isEmpty());
    }

    @Test
    public void testRemoveQuest() {
        testCreateQuest();
        PlayerMock player = server.addPlayer();
        player.setOp(true);
        player.performCommand("quest admin");
        player.simulateInventoryClick(player.getOpenInventory(), ClickType.MIDDLE, 10);
        assertTrue(questPlugin.getQuestConfig().getAllQuests().isEmpty());
    }

    @Test
    public void testEditPermission() {
        testCreateQuest();
        PlayerMock player = server.addPlayer();
        player.setOp(true);
        player.performCommand("quest admin");
        player.simulateInventoryClick(10);
        player.simulateInventoryClick(13);
        String newPermission = TestUtil.randomStr();
        player.chat(newPermission);
        TestUtil.waitTick();
        assertEquals(newPermission, questPlugin.getQuestConfig().getQuest("test").permission());
    }

    @Test
    public void testEditType() {
        testCreateQuest();
        PlayerMock player = server.addPlayer();
        player.setOp(true);
        player.performCommand("quest admin");
        player.simulateInventoryClick(10);
        player.simulateInventoryClick(21);
        player.chat(QuestType.values()[1].name());
        TestUtil.waitTick();
        assertEquals(QuestType.values()[1], questPlugin.getQuestConfig().getQuest("test").type());
    }

    @Test
    public void testEditIcon() {
        testCreateQuest();
        PlayerMock player = server.addPlayer();
        Material material = Material.values()[TestUtil.randomInt(0, Material.values().length - 1)];
        player.setOp(true);
        player.performCommand("quest admin");
        player.simulateInventoryClick(10);
        player.simulateInventoryClick(22);
        player.chat(material.name());
        TestUtil.waitTick();
        assertEquals(material, questPlugin.getQuestConfig().getQuest("test").icon());
    }

    @Test
    public void testEditDescription() {
        testCreateQuest();
        PlayerMock player = server.addPlayer();
        String description = TestUtil.randomStr();
        player.setOp(true);
        player.performCommand("quest admin");
        player.simulateInventoryClick(10);
        player.simulateInventoryClick(23);
        player.chat(description);
        TestUtil.waitTick();
        assertEquals(description, questPlugin.getQuestConfig().getQuest("test").description());
    }

    @Test
    public void testEditRequired() {
        testCreateQuest();
        PlayerMock player = server.addPlayer();
        BigDecimal required = BigDecimal.valueOf(TestUtil.randomInt(0, 100));
        player.setOp(true);
        player.performCommand("quest admin");
        player.simulateInventoryClick(10);
        player.simulateInventoryClick(30);
        player.chat(String.valueOf(required));
        TestUtil.waitTick();
        assertEquals(required, questPlugin.getQuestConfig().getQuest("test").requiredAmount());
    }

    @Test
    public void testEditTarget() {
        testCreateQuest();
        PlayerMock player = server.addPlayer();
        String target = TestUtil.randomStr();
        player.setOp(true);
        player.performCommand("quest admin");
        player.simulateInventoryClick(10);
        player.simulateInventoryClick(31);
        player.chat(target);
        TestUtil.waitTick();
        assertEquals(target, questPlugin.getQuestConfig().getQuest("test").target());
    }

    @Test
    public void testAddReward() {
        testCreateQuest();
        PlayerMock player = server.addPlayer();
        player.setOp(true);
        String randomStr = TestUtil.randomStr();
        player.performCommand("quest add-reward test");
        TestUtil.waitTick();
        player.chat(randomStr);
        TestUtil.waitTick();
        assertFalse(questPlugin.getQuestConfig().getQuest("test").rewardCommands().isEmpty());
        assertEquals(randomStr, questPlugin.getQuestConfig().getQuest("test").rewardCommands().get(0));
    }

    @Test
    public void testRemoveReward() {
        testAddReward();
        PlayerMock player = server.addPlayer();
        player.setOp(true);
        player.performCommand("quest remove-reward test 0");
        TestUtil.waitTick();
        assertTrue(questPlugin.getQuestConfig().getQuest("test").rewardCommands().isEmpty());
    }

    @Test
    public void testEditExpiry() {
        testCreateQuest();
        PlayerMock player = server.addPlayer();
        String expiryStr = "1d";
        long expiry = LanguageService.parseExpiry(expiryStr);
        player.setOp(true);
        player.performCommand("quest admin");
        player.simulateInventoryClick(10);
        player.simulateInventoryClick(40);
        player.chat(expiryStr);
        TestUtil.waitTick();
        assertEquals(expiry, questPlugin.getQuestConfig().getQuest("test").expiry());
    }
}
