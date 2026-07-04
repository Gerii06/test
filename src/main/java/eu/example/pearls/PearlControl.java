package eu.example.pearls;

import eu.example.pearls.command.PearlCommand;
import eu.example.pearls.hook.PlaceholderHook;
import eu.example.pearls.listener.PearlListener;
import eu.example.pearls.manager.CooldownManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class PearlControl extends JavaPlugin {

    private final CooldownManager cooldownManager = new CooldownManager();
    private Settings settings;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadSettings();

        getServer().getPluginManager().registerEvents(new PearlListener(this), this);

        PearlCommand command = new PearlCommand(this);
        if (getCommand("pearlcontrol") != null) {
            getCommand("pearlcontrol").setExecutor(command);
            getCommand("pearlcontrol").setTabCompleter(command);
        }

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderHook(this).register();
            getLogger().info("Hooked into PlaceholderAPI.");
        }

        getLogger().info("PearlControl enabled (targeting Paper 1.21.x).");
    }

    @Override
    public void onDisable() {
        cooldownManager.clearAll();
    }

    /**
     * Re-read config.yml from disk and rebuild the cached settings.
     */
    public void reloadSettings() {
        reloadConfig();
        this.settings = Settings.load(getConfig(), getLogger()::warning);
    }

    public CooldownManager cooldowns() {
        return cooldownManager;
    }

    public Settings settings() {
        return settings;
    }

    /**
     * Immutable snapshot of config values so the hot path never touches the raw
     * config map. Rebuilt on reload.
     */
    public static final class Settings {

        public enum Display {CHAT, ACTIONBAR, TITLE, NONE}

        public double cooldownSeconds;
        public Display cooldownDisplay;
        public String cooldownMessage;

        public boolean denySoundEnabled;
        public String denySound;
        public float denySoundVolume;
        public float denySoundPitch;

        public boolean throwSoundEnabled;
        public String throwSound;
        public float throwSoundVolume;
        public float throwSoundPitch;

        public boolean antiGlitchEnabled;
        public boolean antiGlitchCancel;
        public String antiGlitchMessage;

        public boolean blockCrossWorld;
        public Set<String> disabledWorlds;

        public boolean refundEnabled;
        public boolean refundResetCooldown;
        public String refundMessage;

        public String msgNoPermission;
        public String msgReloaded;
        public String prefix;

        static Settings load(FileConfiguration c, java.util.function.Consumer<String> warn) {
            Settings s = new Settings();
            s.cooldownSeconds = Math.max(0.0, c.getDouble("cooldown", 15.0));
            s.cooldownDisplay = parseDisplay(c.getString("cooldown-display", "ACTIONBAR"), warn);
            s.cooldownMessage = c.getString("cooldown-message", "&cYou must wait &e%seconds%s&c.");

            s.denySoundEnabled = c.getBoolean("deny-sound.enabled", true);
            s.denySound = normalizeSound(c.getString("deny-sound.sound", "ENTITY_VILLAGER_NO"));
            s.denySoundVolume = (float) c.getDouble("deny-sound.volume", 1.0);
            s.denySoundPitch = (float) c.getDouble("deny-sound.pitch", 1.0);

            s.throwSoundEnabled = c.getBoolean("throw-sound.enabled", false);
            s.throwSound = normalizeSound(c.getString("throw-sound.sound", "ENTITY_ENDER_DRAGON_FLAP"));
            s.throwSoundVolume = (float) c.getDouble("throw-sound.volume", 1.0);
            s.throwSoundPitch = (float) c.getDouble("throw-sound.pitch", 1.0);

            s.antiGlitchEnabled = c.getBoolean("anti-glitch.enabled", true);
            s.antiGlitchCancel = c.getBoolean("anti-glitch.cancel-teleport", true);
            s.antiGlitchMessage = c.getString("anti-glitch.message", "");

            s.blockCrossWorld = c.getBoolean("block-cross-world", false);
            s.disabledWorlds = new HashSet<>(c.getStringList("disabled-worlds"));

            s.refundEnabled = c.getBoolean("refund.enabled", true);
            s.refundResetCooldown = c.getBoolean("refund.reset-cooldown", true);
            s.refundMessage = c.getString("refund.message", "");

            s.msgNoPermission = c.getString("messages.no-permission", "&cNo permission.");
            s.msgReloaded = c.getString("messages.reloaded", "&aReloaded.");
            s.prefix = c.getString("messages.prefix", "");
            return s;
        }

        private static Display parseDisplay(String raw, java.util.function.Consumer<String> warn) {
            try {
                return Display.valueOf(raw.toUpperCase(Locale.ROOT).trim());
            } catch (IllegalArgumentException ex) {
                warn.accept("Invalid cooldown-display '" + raw + "', falling back to ACTIONBAR.");
                return Display.ACTIONBAR;
            }
        }

        /**
         * Normalize a config sound name into the namespaced string form accepted by
         * {@code Player#playSound(Location, String, float, float)}. This avoids the
         * {@code org.bukkit.Sound} type, which changed from an enum to a Registry
         * interface in Paper 1.21.3+ (breaking {@code Sound.valueOf}).
         * Accepts both "ENTITY_VILLAGER_NO" and "entity.villager.no" / namespaced keys.
         */
        private static String normalizeSound(String raw) {
            if (raw == null || raw.trim().isEmpty()) {
                return null;
            }
            String value = raw.trim().toLowerCase(Locale.ROOT);
            if (value.contains(":")) {
                return value; // already a namespaced key, e.g. minecraft:entity.villager.no
            }
            if (value.indexOf('.') < 0) {
                value = value.replace('_', '.'); // ENTITY_VILLAGER_NO -> entity.villager.no
            }
            return value;
        }
    }
}
