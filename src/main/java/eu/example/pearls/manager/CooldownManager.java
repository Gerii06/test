package eu.example.pearls.manager;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks per-player pearl cooldowns. Stores the epoch millisecond timestamp at
 * which the player's cooldown expires.
 */
public final class CooldownManager {

    private final ConcurrentHashMap<UUID, Long> expiry = new ConcurrentHashMap<>();

    /**
     * Apply a cooldown of the given number of seconds to the player.
     */
    public void apply(UUID player, double seconds) {
        long millis = Math.max(0L, (long) (seconds * 1000.0));
        expiry.put(player, System.currentTimeMillis() + millis);
    }

    /**
     * Remaining cooldown in seconds, or 0 if the player is not on cooldown.
     */
    public double remaining(UUID player) {
        Long end = expiry.get(player);
        if (end == null) {
            return 0.0;
        }
        long diff = end - System.currentTimeMillis();
        if (diff <= 0L) {
            expiry.remove(player);
            return 0.0;
        }
        return diff / 1000.0;
    }

    public boolean isOnCooldown(UUID player) {
        return remaining(player) > 0.0;
    }

    public void clear(UUID player) {
        expiry.remove(player);
    }

    public void clearAll() {
        expiry.clear();
    }
}
