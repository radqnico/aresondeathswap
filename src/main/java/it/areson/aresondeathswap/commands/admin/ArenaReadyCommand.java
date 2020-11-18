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
public class ArenaReadyCommand implements CommandExecutor {

    private final AresonDeathSwap aresonDeathSwap;
    private final FileManager dataFile;

    public ArenaReadyCommand(AresonDeathSwap plugin, FileManager fileManager) {
        aresonDeathSwap = plugin;
        dataFile = fileManager;

        PluginCommand pluginCommand = aresonDeathSwap.getCommand("arenaReady");
        if (!Objects.isNull(pluginCommand)) {
            pluginCommand.setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] arguments) {
        if (commandSender instanceof Player) {
            if (arguments.length > 0) {
                try {
                    Player player = (Player) commandSender;
                    World locationWorld = player.getWorld();
                    String worldName = locationWorld.getName();

                    if (!worldName.equalsIgnoreCase(aresonDeathSwap.MAIN_WORLD_NAME)) {
                        int minPlayers = Integer.parseInt(arguments[0]);
                        if (minPlayers > 1) {
                            dataFile.setArenaMinPlayers(worldName, minPlayers);

                            //Kick arena players
                            aresonDeathSwap.kickPlayersFromWorld(worldName);
                            commandSender.sendMessage("Arena creata");

                            locationWorld.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
                            aresonDeathSwap.getServer().unloadWorld(worldName, true);
                            aresonDeathSwap.loadArenaByName(worldName);
                        } else {
                            commandSender.sendMessage("Numero giocatori minimi non valido");
                        }

                    } else {
                        commandSender.sendMessage("Sei nel mondo principale");
                    }
                } catch (NumberFormatException exception) {
                    commandSender.sendMessage("Il numero minimo di players deve essere un numero");
                }
            } else {
                commandSender.sendMessage("Inserisci il numero minimo di players");
            }

        } else {
            commandSender.sendMessage("Comando eseguibile solo da player");
        }

        return true;
    }

}
