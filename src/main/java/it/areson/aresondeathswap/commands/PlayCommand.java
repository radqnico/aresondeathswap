package it.areson.aresondeathswap.commands;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.utils.StringPair;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlayCommand implements CommandExecutor, TabCompleter {

    private final AresonDeathSwap aresonDeathSwap;

    public PlayCommand(AresonDeathSwap plugin) {
        aresonDeathSwap = plugin;

        PluginCommand pluginCommand = aresonDeathSwap.getCommand("play");
        if (!Objects.isNull(pluginCommand)) {
            pluginCommand.setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] arguments) {

        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (arguments.length > 0) {
                String arenaName = arguments[0];
                if (aresonDeathSwap.arenas.containsKey(arenaName)) {
                    if(!aresonDeathSwap.playerIsInAnArena(player)) {
                        if (aresonDeathSwap.arenas.get(arenaName).addPlayer(player)) {
                            aresonDeathSwap.messages.sendPlainMessage(player, "arena-join", StringPair.of("%arena%", arenaName));
                            aresonDeathSwap.sounds.joinArena(player);
                            aresonDeathSwap.effects.joinedArena(player);
                            aresonDeathSwap.titles.sendShortTitle(player, "joinarena");
                        } else {
                            aresonDeathSwap.messages.sendPlainMessage(player, "arena-already-started");
                        }
                    } else {
                        aresonDeathSwap.messages.sendPlainMessage(player, "already-in-an-arena");
                    }
                } else {
                    aresonDeathSwap.messages.sendPlainMessage(player, "arena-not-found");
                    aresonDeathSwap.sounds.cannotJoinArena(player);
                }
            } else {
                aresonDeathSwap.messages.sendPlainMessage(player, "missing-arena-name");
                aresonDeathSwap.sounds.cannotJoinArena(player);
            }
        } else {
            commandSender.sendMessage("Comando eseguibile solo da player");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] arguments) {
        List<String> suggestions = new ArrayList<>();
        if (arguments.length == 1) {
            suggestions.addAll(aresonDeathSwap.arenas.keySet());
        }

        return suggestions;
    }

}
