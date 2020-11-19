package it.areson.aresondeathswap.commands.admin;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.managers.FileManager;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.util.Objects;

@SuppressWarnings("NullableProblems")
public class SetSpawnCommand implements CommandExecutor {

    private final AresonDeathSwap aresonDeathSwap;
    private final FileManager dataFile;

    public SetSpawnCommand(AresonDeathSwap plugin, FileManager fileManager) {
        aresonDeathSwap = plugin;
        dataFile = fileManager;

        PluginCommand pluginCommand = aresonDeathSwap.getCommand("setSpawn");
        if (!Objects.isNull(pluginCommand)) {
            pluginCommand.setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] arguments) {
        if (commandSender instanceof Player) {
            Location playerLocation = ((Player) commandSender).getLocation();
            World locationWorld = playerLocation.getWorld();
            if (locationWorld != null) {
                locationWorld.setSpawnLocation(playerLocation);
                dataFile.setLocation(playerLocation, "lobby-spawn");
                commandSender.sendMessage("Spawn impostato");
            } else {
                commandSender.sendMessage("Errore nell'ottenimento del mondo");
            }
        } else {
            commandSender.sendMessage("Comando eseguibile solo da player");
        }

        return true;
    }

}
