package it.areson.aresondeathswap.commands.arena;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.arena.Arena;
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
import java.util.Optional;

@AresonCommand("delete")
public class PrepareCommand extends CommandParserCommand {

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
        Optional<Arena> arenaByName = arenaManager.getArenaByName(arenaName);
        if(arenaByName.isPresent()){
            Arena arena = arenaByName.get();
            arena.open();
        }

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
