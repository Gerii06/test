package eu.example.pearls.hook;

import eu.example.pearls.PearlControl;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Exposes pearl cooldown data to PlaceholderAPI. Only instantiated when
 * PlaceholderAPI is present, so it is safe to reference its API here.
 *
 * Placeholders:
 *   %pearlcontrol_cooldown%      -> remaining seconds, one decimal
 *   %pearlcontrol_cooldown_int%  -> remaining seconds, rounded up
 *   %pearlcontrol_on_cooldown%   -> true / false
 */
public final class PlaceholderHook extends PlaceholderExpansion {

    private final PearlControl plugin;

    public PlaceholderHook(PearlControl plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "pearlcontrol";
    }

    @Override
    public @NotNull String getAuthor() {
        return "PearlControl";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }
        double remaining = plugin.cooldowns().remaining(player.getUniqueId());
        return switch (params.toLowerCase(Locale.ROOT)) {
            case "cooldown" -> String.format(Locale.US, "%.1f", remaining);
            case "cooldown_int" -> String.valueOf((int) Math.ceil(remaining));
            case "on_cooldown" -> String.valueOf(remaining > 0.0);
            default -> null;
        };
    }
}
