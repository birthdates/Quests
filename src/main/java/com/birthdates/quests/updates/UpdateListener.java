package com.birthdates.quests.updates;

import com.birthdates.quests.config.QuestConfig;
import com.birthdates.quests.lang.LanguageService;
import org.bukkit.configuration.ConfigurationSection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Listener for updates to the quest config and language (using Redis pub sub)
 */
public class UpdateListener {

    private final JedisPubSub pubSub;
    private final JedisPool jedisPool;
    private final ConfigurationSection redisConfig;
    private final Jedis jedis;

    public UpdateListener(QuestConfig config, ConfigurationSection redisConfig, LanguageService languageService) {
        if (redisConfig == null) {
            throw new IllegalStateException("Expected redis section in config, not found");
        }

        this.redisConfig = redisConfig;
        jedisPool = new JedisPool(redisConfig.getString("Host"), redisConfig.getInt("Port"));
        this.pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String value) {
                // Message will be formatted as "KEY$VALUE"
                String[] split = value.split("\\$");
                String key = split[0];
                value = split[1];

                // Handle quest or language update
                if (key.equals("QUEST_CONFIG")) {
                    config.invalidateCache(value);
                    config.getQuest(value);
                    return;
                }
                languageService.update(key, value);
            }
        };

        jedis = getJedis();
        new Thread(() -> {
            try {
                getJedis().subscribe(pubSub, "QUEST_UPDATE");
            } catch (JedisConnectionException ignored) {
                // Thrown when the connection is closed (e.g. server shutdown)
            }
        }, "Quest Config Redis Listener").start();
    }

    /**
     * Get Jedis instance from pool (authenticated and selected database)
     *
     * @return {@link Jedis}
     */
    private Jedis getJedis() {
        Jedis jedis = jedisPool.getResource();
        String password = redisConfig.getString("Password");
        int database = redisConfig.getInt("Database");
        if (password != null && !password.isBlank()) jedis.auth(password);
        jedis.select(database);
        return jedis;
    }

    /**
     * Send a quest update to the redis server
     *
     * @param questID The quest ID
     */
    public void sendQuestUpdate(String questID) {
        sendUpdate("QUEST_CONFIG", questID);
    }

    /**
     * Send a specific update to the redis server
     *
     * @param channel Update type
     * @param key     Update key (what to update)
     */
    public void sendUpdate(String channel, String key) {
        jedis.publish("QUEST_UPDATE", channel + "$" + key);
    }

    public void unload() {
        jedisPool.close();
    }
}
