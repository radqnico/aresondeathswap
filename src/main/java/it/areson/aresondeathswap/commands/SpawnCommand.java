package it.areson.aresondeathswap.commands;

import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class SpawnCommand implements CommandExecutor {

    private final AresonDeathSwap aresonDeathSwap;


    public SpawnCommand(AresonDeathSwap plugin) {
        aresonDeathSwap = plugin;

        PluginCommand pluginCommand = plugin.getCommand("spawn");
        if (!Objects.isNull(pluginCommand)) {
            pluginCommand.setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] arguments) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (aresonDeathSwap.playerIsInAnArena(player)) {
                aresonDeathSwap.removePlayerFromArenas(player);
            }
            aresonDeathSwap.teleportToLobbySpawn(player);
        } else {
            commandSender.sendMessage("Comando eseguibile solo da player");
        }

        return true;
    }
}
