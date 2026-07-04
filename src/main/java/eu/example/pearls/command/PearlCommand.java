package eu.example.pearls.command;

import eu.example.pearls.PearlControl;
import eu.example.pearls.util.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public final class PearlCommand implements CommandExecutor, TabCompleter {

    private static final String PERM_ADMIN = "pearlcontrol.admin";

    private final PearlControl plugin;

    public PearlCommand(PearlControl plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        PearlControl.Settings s = plugin.settings();

        if (!sender.hasPermission(PERM_ADMIN)) {
            sender.sendMessage(Text.parse(s.prefix + s.msgNoPermission));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> {
                plugin.reloadSettings();
                sender.sendMessage(Text.parse(plugin.settings().prefix + plugin.settings().msgReloaded));
            }
            case "help" -> sendHelp(sender, label);
            default -> sendHelp(sender, label);
        }
        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(Text.parse("&8&m----------------------------------"));
        sender.sendMessage(Text.parse("&d PearlControl &7commands:"));
        sender.sendMessage(Text.parse("&e /" + label + " reload &7- reload the config"));
        sender.sendMessage(Text.parse("&e /" + label + " help &7- show this help"));
        sender.sendMessage(Text.parse("&8&m----------------------------------"));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission(PERM_ADMIN)) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            return List.of("reload", "help").stream()
                    .filter(o -> o.startsWith(prefix))
                    .toList();
        }
        return List.of();
    }
}
