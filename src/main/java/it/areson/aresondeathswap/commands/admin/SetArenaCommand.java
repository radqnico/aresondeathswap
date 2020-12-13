package it.areson.aresondeathswap.commands.admin;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.exceptions.PropertyDoesNotExistException;
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
    private final MultiverseCore multiverseCore;

    public SetArenaCommand(AresonDeathSwap plugin, FileManager dataFile, MultiverseCore multiverseCore) {
        this.aresonDeathSwap = plugin;
        this.dataFile = dataFile;
        this.multiverseCore = multiverseCore;

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

                    Block highestBlock = locationWorld.getHighestBlockAt(0, 0);
                    locationWorld.setSpawnLocation(highestBlock.getLocation());

                    dataFile.addArena(locationWorld.getName());

                    locationWorld.getWorldBorder().setCenter(0, 0);
                    locationWorld.getWorldBorder().setSize(3500);

                    locationWorld.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);

                    try {
                        multiverseCore.getMVWorldManager().getMVWorld(locationWorld).setPropertyValue("autoLoad", "false");

                        if (multiverseCore.getMVWorldManager().unloadWorld(worldName, true)) {
                            aresonDeathSwap.loadArenaByName(worldName);
                        } else {
                            aresonDeathSwap.getLogger().severe("Error while deleting MultiVerse world " + worldName);
                        }

                        commandSender.sendMessage("Arena creata");
                    } catch (PropertyDoesNotExistException e) {
                        e.printStackTrace();
                    }
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
