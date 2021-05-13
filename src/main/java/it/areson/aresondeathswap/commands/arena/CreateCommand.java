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

@AresonCommand("create")
public class CreateCommand extends CommandParserCommand {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length != 4) {
            commandSender.sendMessage("Command '/arena create' usage: " + command.getUsage());
            return true;
        }
        String arenaName = strings[1];
        String worldName = strings[2];
        String stringMinPlayers = strings[3];
        int minPlayers = 0;
        try {
            minPlayers = Integer.parseInt(stringMinPlayers);
        } catch (NumberFormatException exception) {
            commandSender.sendMessage("Minimum players was not an integer");
            return true;
        }

        ArenaManager arenaManager = AresonDeathSwap.instance.getArenaManager();
        Arena newArena = arenaManager.createNewArenaAndLoadWorld(AresonDeathSwap.instance, arenaName, worldName, minPlayers);

        commandSender.sendMessage("Created arena " + arenaName + " and loaded its world");

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> suggestions = new ArrayList<>();
        switch (strings.length) {
            case 2:
                suggestions.add("arenaName");
                break;
            case 3:
                suggestions.add("arenaWorld");
                break;
            case 4:
                suggestions.add("minPlayers");
                break;
        }
        return suggestions;
    }

}
