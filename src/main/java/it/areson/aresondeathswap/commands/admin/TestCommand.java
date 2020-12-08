package it.areson.aresondeathswap.commands.admin;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.loadbalance.LoadBalancer;
import it.areson.aresondeathswap.loadbalance.PlaceBlockJob;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TestCommand implements CommandExecutor, TabCompleter {

    private final AresonDeathSwap aresonDeathSwap;

    public TestCommand(AresonDeathSwap plugin) {
        aresonDeathSwap = plugin;

        PluginCommand pluginCommand = plugin.getCommand("test");
        if (!Objects.isNull(pluginCommand)) {
            pluginCommand.setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] arguments) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            Location location = player.getLocation();
            LoadBalancer loadBalancer = new LoadBalancer("place stones");
            for (int x = 0; x < 100; x++) {
                for (int y = 0; y < 100; y++) {
                    for (int z = 0; z < 100; z++) {
                        loadBalancer.addJob(new PlaceBlockJob(player.getLocation().clone().add(x, y, z), Material.STONE));
                    }
                }
            }
            player.sendMessage("Start job");
            loadBalancer.start(aresonDeathSwap).whenComplete((totalTicks, exception) -> {
                player.sendMessage("Job took " + totalTicks + "ticks");
            });
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
