package com.birthdates.quests;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import org.bukkit.event.inventory.ClickType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LanguageServiceTests {

    private QuestPlugin questPlugin;
    private PlayerMock player;
    private String language, firstKey;

    @BeforeEach
    public void setUp() {
        ServerMock server = MockBukkit.mock();
        questPlugin = MockBukkit.load(QuestPlugin.class);
        player = server.addPlayer();
        player.setOp(true);
        language = questPlugin.getLanguageService().getAvailableLanguages().stream().findFirst().orElse("en");
        firstKey = questPlugin.getLanguageService().getLanguageMap(language).keySet().stream().findFirst().orElse(null);
        assertNotNull(firstKey);
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testDefaults() {
        assertFalse(questPlugin.getLanguageService().getAvailableLanguages().isEmpty());
    }

    @Test
    public void testEditEntry() {
        player.performCommand("lang");
        player.simulateInventoryClick(0);
        player.simulateInventoryClick(10);
        String newStr = TestUtil.randomStr();
        player.chat(newStr);
        TestUtil.waitTick();
        assertEquals(newStr, questPlugin.getLanguageService().getLanguageMap(language).get(firstKey));
    }

    @Test
    public void testDeleteEntry() {
        player.performCommand("lang");
        player.simulateInventoryClick(0);
        player.simulateInventoryClick(player.getOpenInventory(), ClickType.MIDDLE, 10);
        assertNull(questPlugin.getLanguageService().getLanguageMap(language).get(firstKey));
    }

    @Test
    public void testCreateEntry() {
        player.performCommand("lang");
        player.simulateInventoryClick(0);
        player.simulateInventoryClick(4);
        String newKey = TestUtil.randomStr();
        String newStr = TestUtil.randomStr();
        player.chat(newKey);
        TestUtil.waitTick();
        player.chat(newStr);
        TestUtil.waitTick();
        assertEquals(newStr, questPlugin.getLanguageService().getLanguageMap(language).get(newKey));
    }
}
