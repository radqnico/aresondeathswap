package it.areson.aresondeathswap.commands.admin;

import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class TestChestCommand implements CommandExecutor, TabCompleter {

    private final AresonDeathSwap aresonDeathSwap;


    public TestChestCommand(AresonDeathSwap plugin) {
        aresonDeathSwap = plugin;

        PluginCommand pluginCommand = plugin.getCommand("testChest");
        if (!Objects.isNull(pluginCommand)) {
            pluginCommand.setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] arguments) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            aresonDeathSwap.loot.placeNewChestNear(player);
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
