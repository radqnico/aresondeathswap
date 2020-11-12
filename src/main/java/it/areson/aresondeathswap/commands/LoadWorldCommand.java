package it.areson.aresondeathswap.commands;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoadWorldCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] arguments) {

        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;

            if(arguments.length > 0) {
                World loadedWorld = new WorldCreator(arguments[0]).createWorld();

                if(loadedWorld != null) {
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

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return new ArrayList<>(Collections.singleton("setArena"));
    }
}
