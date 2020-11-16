package it.areson.aresondeathswap.commands.admin;

import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.util.Objects;

public class LoadWorldCommand implements CommandExecutor {

    public LoadWorldCommand(AresonDeathSwap plugin) {
        PluginCommand pluginCommand = plugin.getCommand("loadWorld");
        if (!Objects.isNull(pluginCommand)) {
            pluginCommand.setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] arguments) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;

            if (arguments.length > 0) {
                World loadedWorld = new WorldCreator(arguments[0]).createWorld();

                if (loadedWorld != null) {
                    player.teleport(loadedWorld.getSpawnLocation());
                    player.sendMessage("Mondo caricato");
                } else {
                    player.sendMessage("Errore nel caricamento del mondo");
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
