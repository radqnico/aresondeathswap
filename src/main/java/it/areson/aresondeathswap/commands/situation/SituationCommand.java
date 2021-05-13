package it.areson.aresondeathswap.commands.situation;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.arena.Arena;
import it.areson.aresondeathswap.arena.ArenaManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SituationCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        StringBuilder out = new StringBuilder();
        ArenaManager arenaManager = AresonDeathSwap.instance.getArenaManager();
        Collection<Arena> arenas = arenaManager.getArenas().values();
        for (Arena arena : arenas) {
            out.append(arena.getArenaName()).append(": ").append(arena.getArenaStatus().name()).append("|").append(arena.getPlayers().size()).append(";");
        }
        commandSender.sendMessage(out.toString());
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }

}
