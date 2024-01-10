package com.birthdates.quests.sign.impl;

import com.birthdates.quests.sign.QuestSign;
import com.birthdates.quests.sign.SignService;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A mock implementation of {@link SignService} for testing purposes.
 */
public class MockSignService implements SignService {
    private final Set<QuestSign> signs = new HashSet<>();

    @Override
    public Collection<QuestSign> getAllSignLocations() {
        return signs;
    }

    @Override
    public boolean addSignLocation(QuestSign sign) {
        return signs.add(sign);
    }

    @Override
    public boolean removeSignLocation(@NotNull Location location) {
        return signs.removeIf(x -> x.location().equals(location));
    }

    @Override
    public QuestSign getSign(Location location) {
        return signs.stream().filter(x -> x.location().equals(location)).findFirst().orElse(null);
    }

    @Override
    public void handleUpdate(String id) {
        // no-op
    }
}
