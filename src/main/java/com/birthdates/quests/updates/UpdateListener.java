package com.birthdates.quests.updates;

import com.birthdates.quests.config.QuestConfig;
import com.birthdates.quests.lang.LanguageService;
import org.bukkit.configuration.ConfigurationSection;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

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
                String[] split = value.split("\\$");
                String key = split[0];
                value = split[1];
                if (key.equals("QUEST_CONFIG")) {
                    config.invalidate(value);
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

    private Jedis getJedis() {
        Jedis jedis = jedisPool.getResource();
        String password = redisConfig.getString("Password");
        int database = redisConfig.getInt("Database");
        if (password != null && !password.isBlank()) jedis.auth(password);
        jedis.select(database);
        return jedis;
    }

    public void sendUpdate(String questID) {
        sendUpdate("QUEST_CONFIG", questID);
    }

    public void sendUpdate(String channel, String key) {
        jedis.publish("QUEST_UPDATE", channel + "$" + key);
    }

    public void unload() {
        jedisPool.close();
    }
}
