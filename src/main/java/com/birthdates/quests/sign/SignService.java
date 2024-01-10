package com.birthdates.quests.sign;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A service to manage sign locations
 */
public interface SignService {
    Collection<QuestSign> getAllSignLocations();

    /**
     * Adds a sign location
     *
     * @param sign the sign location to add
     * @return true if the location was added, false otherwise (already exists)
     */
    boolean addSignLocation(QuestSign sign);

    /**
     * Removes a sign location
     *
     * @param location the location to remove
     * @return true if the location was removed, false otherwise (doesn't exist)
     */
    boolean removeSignLocation(@NotNull Location location);

    QuestSign getSign(Location location);

    default boolean isSign(Location location) {
        return getSign(location) != null;
    }

    /**
     * Handle the update of a quest sign
     *
     * @param id Location of sign serialized
     */
    void handleUpdate(String id);
}
