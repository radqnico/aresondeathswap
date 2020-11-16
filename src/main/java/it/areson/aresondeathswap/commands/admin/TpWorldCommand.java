package it.areson.aresondeathswap.commands.admin;

import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TpWorldCommand implements CommandExecutor {

    private final AresonDeathSwap aresonDeathSwap;


    public TpWorldCommand(AresonDeathSwap plugin) {
        aresonDeathSwap = plugin;

        PluginCommand pluginCommand = plugin.getCommand("tpWorld");
        if (!Objects.isNull(pluginCommand)) {
            pluginCommand.setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] arguments) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            if (arguments.length > 0) {
                String worldName = arguments[0];
                List<String> worldNames = aresonDeathSwap.getServer().getWorlds().find;

                if (worldNames.contains(worldName)) {
                    player.teleport(loadedWorld.getSpawnLocation());
                    player.sendMessage("Mondo caricato");
                } else {
                    player.sendMessage("Mondo non trovato");
                }
            } else {
                player.sendMessage("Manca il nome del mondo");
            }
        } else {
            commandSender.sendMessage("Comando eseguibile solo da player");
        }

        return true;
    }
}
