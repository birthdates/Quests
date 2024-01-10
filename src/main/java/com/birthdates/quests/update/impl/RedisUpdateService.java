package com.birthdates.quests.update.impl;

import com.birthdates.quests.config.QuestConfig;
import com.birthdates.quests.lang.LanguageService;
import com.birthdates.quests.sign.SignService;
import com.birthdates.quests.update.UpdateService;
import com.birthdates.quests.update.UpdateType;
import org.bukkit.configuration.ConfigurationSection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Listener for updates to the quest config and language (using Redis pub sub)
 */
public class RedisUpdateService extends UpdateService {
    private final JedisPool jedisPool;
    private final ConfigurationSection redisConfig;
    private final Jedis jedis;

    public RedisUpdateService(QuestConfig config, ConfigurationSection redisConfig, LanguageService languageService, SignService signService) {
        super(config, languageService, signService);
        if (redisConfig == null) {
            throw new IllegalStateException("Expected redis section in config, not found");
        }

        this.redisConfig = redisConfig;
        jedisPool = new JedisPool(redisConfig.getString("Host"), redisConfig.getInt("Port"));
        jedis = getJedis();

        // Setup pub sub listener
        JedisPubSub pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String value) {
                String[] split = value.split("\\$");
                String key = split[0];
                handleUpdate(key, split[1]);
            }
        };
        new Thread(() -> {
            // Different Jedis instance required for subscribing / publishing
            try (Jedis sub = getJedis()) {
                sub.subscribe(pubSub, "QUEST_UPDATE");
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
        Jedis jedis;
        try {
            jedis = jedisPool.getResource();
        } catch (JedisConnectionException exception) {
            throw new IllegalStateException("Failed to connect to Redis server", exception);
        }
        String password = redisConfig.getString("Password");
        int database = redisConfig.getInt("Database");
        if (password != null && !password.isBlank()) jedis.auth(password);
        jedis.select(database);
        return jedis;
    }

    /**
     * Send a specific update to the redis pub sub
     *
     * @param type Update type
     * @param key  Update key (what to update)
     */
    public void sendUpdate(UpdateType type, String key) {
        jedis.publish("QUEST_UPDATE", type.getChannel() + "$" + key);
    }

    public void unload() {
        jedisPool.close();
    }
}
