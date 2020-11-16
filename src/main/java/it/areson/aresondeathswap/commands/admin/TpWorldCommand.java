package it.areson.aresondeathswap.commands.admin;

import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Optional;

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
                Optional<World> searchedWorld = aresonDeathSwap.getServer().getWorlds().stream().filter(world -> world.getName().equals(worldName)).findFirst();

                if (searchedWorld.isPresent()) {
                    player.teleport(searchedWorld.get().getSpawnLocation());
                    player.sendMessage("Teletrasportato");
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
