package it.areson.aresondeathswap.commands.arena;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.arena.ArenaManager;
import it.areson.aresondeathswap.commands.AresonCommand;
import it.areson.aresondeathswap.commands.CommandParserCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@AresonCommand("delete")
public class DeleteCommand extends CommandParserCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length != 2) {
            commandSender.sendMessage("Command '/arena' usage: " + command.getUsage());
            return true;
        }
        String arenaName = strings[1];

        ArenaManager arenaManager = AresonDeathSwap.instance.getArenaManager();

        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            player.teleport(AresonDeathSwap.instance.getServer().getWorld("world").getSpawnLocation());
        }
        arenaManager.removeArenaAndUnloadWorld(arenaName);

        commandSender.sendMessage("Deleted arena " + arenaName);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> suggestions = new ArrayList<>();
        if (strings.length == 2) {
            ArenaManager arenaManager = AresonDeathSwap.instance.getArenaManager();
            suggestions.addAll(arenaManager.getArenas().keySet());
        }
        return suggestions;
    }

}
