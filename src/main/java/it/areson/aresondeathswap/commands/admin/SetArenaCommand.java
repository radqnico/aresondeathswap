package it.areson.aresondeathswap.commands.admin;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.managers.FileManager;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.util.Objects;

@SuppressWarnings("NullableProblems")
public class SetArenaCommand implements CommandExecutor {

    private final AresonDeathSwap aresonDeathSwap;
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
                if (!worldName.equalsIgnoreCase(aresonDeathSwap.MAIN_WORLD_NAME)) {

                    Block highestBlock = locationWorld.getHighestBlockAt(playerLocation.getBlockX(), playerLocation.getBlockZ());
                    locationWorld.setSpawnLocation(highestBlock.getLocation());

                    dataFile.addArena(playerLocation.getWorld().getName());

                    //Kick arena players
                    aresonDeathSwap.kickPlayersFromWorld(locationWorld.getName());
                    commandSender.sendMessage("Arena creata");

                    locationWorld.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
                    aresonDeathSwap.getServer().unloadWorld(worldName, true);
                    aresonDeathSwap.loadArenaByName(worldName);
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

}
