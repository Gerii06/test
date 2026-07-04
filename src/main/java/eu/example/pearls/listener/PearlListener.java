package eu.example.pearls.listener;

import eu.example.pearls.PearlControl;
import eu.example.pearls.PearlControl.Settings;
import eu.example.pearls.util.Text;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.time.Duration;

public final class PearlListener implements Listener {

    private static final String PERM_BYPASS = "pearlcontrol.bypass";

    private final PearlControl plugin;

    public PearlListener(PearlControl plugin) {
        this.plugin = plugin;
    }

    /**
     * Gate the throw before the pearl item is consumed. Cancelling the interact
     * means the item stays in the player's hand, so there is nothing to refund.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.ENDER_PEARL) {
            return;
        }

        Player player = event.getPlayer();
        Settings s = plugin.settings();

        // Worlds where pearls are disabled outright.
        if (s.disabledWorlds.contains(player.getWorld().getName())) {
            event.setCancelled(true);
            event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
            return;
        }

        if (player.hasPermission(PERM_BYPASS)) {
            return;
        }

        double remaining = plugin.cooldowns().remaining(player.getUniqueId());
        if (remaining > 0.0) {
            event.setCancelled(true);
            event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
            sendCooldown(player, remaining, s);
            playSound(player, s.denySoundEnabled, s.denySound, s.denySoundVolume, s.denySoundPitch);
        }
    }

    /**
     * Record the cooldown and play the throw sound once a pearl actually launches.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity() instanceof EnderPearl pearl)) {
            return;
        }
        ProjectileSource source = pearl.getShooter();
        if (!(source instanceof Player player)) {
            return;
        }
        Settings s = plugin.settings();
        if (player.hasPermission(PERM_BYPASS)) {
            return;
        }
        plugin.cooldowns().apply(player.getUniqueId(), s.cooldownSeconds);
        playSound(player, s.throwSoundEnabled, s.throwSound, s.throwSoundVolume, s.throwSoundPitch);
    }

    /**
     * Anti-glitch and cross-world handling. At this point the pearl has already
     * been consumed, so a blocked teleport refunds the item.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return;
        }
        Player player = event.getPlayer();
        Settings s = plugin.settings();
        Location to = event.getTo();
        if (to == null) {
            return;
        }

        boolean crossWorld = event.getFrom().getWorld() != null
                && to.getWorld() != null
                && !event.getFrom().getWorld().equals(to.getWorld());
        if (s.blockCrossWorld && crossWorld) {
            event.setCancelled(true);
            refund(player, s);
            return;
        }

        if (s.antiGlitchEnabled && s.antiGlitchCancel && wouldGlitch(to)) {
            event.setCancelled(true);
            if (!Text.isBlank(s.antiGlitchMessage)) {
                player.sendMessage(prefixed(s, s.antiGlitchMessage));
            }
            refund(player, s);
        }
    }

    /**
     * True when landing at {@code to} would leave the player embedded in a full
     * solid block (feet or head space) - the classic wall glitch. We require the
     * block to be both solid and occluding (a full opaque cube such as stone or
     * obsidian) so that slabs, stairs, carpets and glass a player can legitimately
     * stand on/in are not flagged as glitches.
     */
    private boolean wouldGlitch(Location to) {
        Block feet = to.getBlock();
        Block head = feet.getRelative(0, 1, 0);
        return isFullSolid(feet) || isFullSolid(head);
    }

    private boolean isFullSolid(Block block) {
        Material type = block.getType();
        return type.isSolid() && type.isOccluding();
    }

    private void refund(Player player, Settings s) {
        if (!s.refundEnabled) {
            return;
        }
        giveOnePearl(player);
        if (s.refundResetCooldown) {
            plugin.cooldowns().clear(player.getUniqueId());
        }
        if (!Text.isBlank(s.refundMessage)) {
            player.sendMessage(prefixed(s, s.refundMessage));
        }
    }

    private void giveOnePearl(Player player) {
        ItemStack pearl = new ItemStack(Material.ENDER_PEARL, 1);
        var leftover = player.getInventory().addItem(pearl);
        // Drop anything that didn't fit at the player's feet.
        leftover.values().forEach(stack ->
                player.getWorld().dropItemNaturally(player.getLocation(), stack));
    }

    private void sendCooldown(Player player, double remaining, Settings s) {
        if (s.cooldownDisplay == Settings.Display.NONE || Text.isBlank(s.cooldownMessage)) {
            return;
        }
        String rendered = s.cooldownMessage
                .replace("%seconds%", String.format(java.util.Locale.US, "%.1f", remaining))
                .replace("%seconds_int%", String.valueOf((int) Math.ceil(remaining)));
        Component component = Text.parse(rendered);

        switch (s.cooldownDisplay) {
            case CHAT -> player.sendMessage(component);
            case ACTIONBAR -> player.sendActionBar(component);
            case TITLE -> player.showTitle(Title.title(
                    Component.empty(),
                    component,
                    Title.Times.times(Duration.ZERO, Duration.ofMillis(1200), Duration.ofMillis(300))));
            default -> {
            }
        }
    }

    private Component prefixed(Settings s, String message) {
        String prefix = s.prefix == null ? "" : s.prefix;
        return Text.parse(prefix + message);
    }

    private void playSound(Player player, boolean enabled, String sound, float volume, float pitch) {
        if (!enabled || Text.isBlank(sound)) {
            return;
        }
        // String overload is stable across 1.21.x (Sound became a Registry interface in 1.21.3+).
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}
