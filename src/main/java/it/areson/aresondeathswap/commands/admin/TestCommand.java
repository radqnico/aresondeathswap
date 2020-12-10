package it.areson.aresondeathswap.commands.admin;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.loadbalancer.LoadBalancer;
import it.areson.aresondeathswap.loadbalancer.PlaceBlockJob;
import it.areson.aresondeathswap.utils.CDTaskSeries;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static it.areson.aresondeathswap.enums.ArenaStatus.InGame;
import static it.areson.aresondeathswap.enums.ArenaStatus.Waiting;

@SuppressWarnings("NullableProblems")
public class TestCommand implements CommandExecutor, TabCompleter {

    private final AresonDeathSwap aresonDeathSwap;

    public TestCommand(AresonDeathSwap plugin) {
        aresonDeathSwap = plugin;

        PluginCommand pluginCommand = plugin.getCommand("testcommand");
        if (!Objects.isNull(pluginCommand)) {
            pluginCommand.setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] arguments) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            new CDTaskSeries(aresonDeathSwap,
                    aresonDeathSwap.STARTING_TIME,
                    () -> {
                        player.sendMessage("ended");
                    },
                    () -> {
                        player.sendMessage("interrupted");
                    },
                    15,
                    aresonDeathSwap.messages.getPlainMessage("countdown-starting-message"),
                    null,
                    aresonDeathSwap.messages.getPlainMessage("countdown-start-message")
            ).start();
        } else {
            commandSender.sendMessage("Comando eseguibile solo da player");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] arguments) {
        List<String> suggestions = new ArrayList<>();
        if (arguments.length == 1) {
            suggestions.addAll(aresonDeathSwap.getServer().getWorlds().stream().map(World::getName).collect(Collectors.toList()));
        }

        return suggestions;
    }
}
