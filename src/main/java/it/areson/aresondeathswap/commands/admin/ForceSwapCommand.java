package it.areson.aresondeathswap.commands.admin;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.loadbalancer.LoadBalancer;
import it.areson.aresondeathswap.loadbalancer.PlaceBlockJob;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("NullableProblems")
public class ForceSwapCommand implements CommandExecutor {

    private final AresonDeathSwap aresonDeathSwap;

    public ForceSwapCommand(AresonDeathSwap plugin) {
        aresonDeathSwap = plugin;

        PluginCommand pluginCommand = plugin.getCommand("forceswap");
        if (!Objects.isNull(pluginCommand)) {
            pluginCommand.setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] arguments) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            Optional<String> arenaNameFromPlayer = aresonDeathSwap.getArenaNameFromPlayer(player);
            if(arenaNameFromPlayer.isPresent()){
                String arenaName = arenaNameFromPlayer.get();
                aresonDeathSwap.arenas.get(arenaName).forceSwap();
                player.sendMessage("Forzato swap");
            }else{
                player.sendMessage("Non sei in gioco in una arena");
            }
        } else {
            commandSender.sendMessage("Comando eseguibile solo da player");
        }

        return true;
    }
}
