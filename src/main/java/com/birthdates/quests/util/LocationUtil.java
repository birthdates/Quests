package com.birthdates.quests.util;

import com.birthdates.quests.QuestPlugin;
import org.bukkit.Location;

public class LocationUtil {
    public static String serializeLocation(Location location) {
        return location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ();
    }

    public static Location deserializeLocation(String string) {
        String[] split = string.split(",");
        return new Location(QuestPlugin.getInstance().getServer().getWorld(split[0]),
                Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3]));
    }
}
