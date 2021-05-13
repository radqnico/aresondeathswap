package it.areson.aresondeathswap.commands.arena;

import it.areson.aresondeathswap.commands.AresonCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@AresonCommand("create")
public class CreateCommand implements CommandExecutor, TabCompleter {

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
        try{
            minPlayers = Integer.parseInt(stringMinPlayers);
        }catch (NumberFormatException exception){
            commandSender.sendMessage("Minimum players was not an integer");
            return true;
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return null;
    }

}
