package it.areson.aresondeathswap.commands.admin;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.managers.FileManager;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("NullableProblems")
public class DeleteArenaCommand implements CommandExecutor, TabExecutor {

    private final AresonDeathSwap aresonDeathSwap;
    private final FileManager dataFile;

    public DeleteArenaCommand(AresonDeathSwap plugin, FileManager fileManager) {
        aresonDeathSwap = plugin;
        dataFile = fileManager;

        PluginCommand pluginCommand = aresonDeathSwap.getCommand("deleteArena");
        if (!Objects.isNull(pluginCommand)) {
            pluginCommand.setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] arguments) {
        if (commandSender instanceof Player) {
            if (arguments.length > 0) {
                String arenaName = arguments[0];
                if (aresonDeathSwap.arenas.containsKey(arenaName)) {
                    aresonDeathSwap.kickPlayersFromWorld(arenaName);
                    aresonDeathSwap.arenas.remove(arenaName);
                    aresonDeathSwap.getServer().unloadWorld(arenaName, false);
                    dataFile.removeArena(arenaName);

                    commandSender.sendMessage("Arena rimossa");
                } else {
                    commandSender.sendMessage("Arena non trovata");
                }
            } else {
                commandSender.sendMessage("Specifica il nome dell'arena");
            }
        } else {
            commandSender.sendMessage("Comando eseguibile solo da player");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] arguments) {
        return new ArrayList<>(aresonDeathSwap.arenas.keySet());
    }
}
