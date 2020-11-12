package it.areson.aresondeathswap.commands;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.FileManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class SetArenaCommand implements CommandExecutor, TabCompleter {

    private AresonDeathSwap aresonDeathSwap;
    private final FileManager dataFile;

    public SetArenaCommand(AresonDeathSwap plugin, FileManager fileManager) {
        aresonDeathSwap = plugin;
        dataFile = fileManager;

        PluginCommand pluginCommand = aresonDeathSwap.getCommand("setArena");
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
                String worldName = locationWorld.getName();
                if (!worldName.equalsIgnoreCase("world")) {
                    locationWorld.setSpawnLocation(playerLocation);
                    dataFile.setArenaLocation(playerLocation);//TODO Remove to array
                    aresonDeathSwap.getServer().getOnlinePlayers().forEach(
                            player -> {
                                if (player.getWorld().getName().equalsIgnoreCase(worldName)) {
                                    player.kickPlayer("Mondo trasformato in arena");
                                }
                            }
                    );
                    aresonDeathSwap.getServer().unloadWorld(worldName, true);
                    aresonDeathSwap.loadArenaWorld(worldName);
                } else {
                    commandSender.sendMessage("Sei nel mondo principale");
                }
            } else {
                commandSender.sendMessage("Errore nell'ottenimento del mondo");
            }
        } else {
            commandSender.sendMessage("Comando eseguibile solo da player");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return new ArrayList<>(Collections.singleton("setArena"));
    }
}
