package it.areson.aresondeathswap.commands.play;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.arena.Arena;
import it.areson.aresondeathswap.arena.ArenaManager;
import it.areson.aresondeathswap.player.DeathswapPlayer;
import it.areson.aresondeathswap.player.DeathswapPlayerManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlayCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (strings.length == 1) {
                String arenaName = strings[0];
                ArenaManager arenaManager = AresonDeathSwap.instance.getArenaManager();
                Optional<Arena> arenaByName = arenaManager.getArenaByName(arenaName);
                if (!arenaByName.isPresent()) {
                    commandSender.sendMessage("L'arena non esiste");
                    return true;
                }
                Arena arena = arenaByName.get();
                addPlayerToArena(arenaManager, arena, player);
            } else {
                commandSender.sendMessage("Specifica l'arena in cui giocare");
            }
        } else {
            commandSender.sendMessage("Command only available by player");
        }
        return true;
    }

    private void addPlayerToArena(ArenaManager arenaManager, Arena arena, Player player) {
        DeathswapPlayerManager deathswapPlayerManager = AresonDeathSwap.instance.getDeathswapPlayerManager();
        DeathswapPlayer deathswapPlayer = deathswapPlayerManager.getDeathswapPlayer(player);

        Optional<Arena> arenaOfPlayer = arenaManager.getArenaOfPlayer(deathswapPlayer);
        if (arenaOfPlayer.isPresent() && arenaOfPlayer.get().equals(arena)) {
            player.sendMessage("Sei già nell'arena " + arena.getArenaName());
            return;
        }

        arenaManager.removePlayerFromAllArenas(deathswapPlayer);
        arena.addPlayer(deathswapPlayer, player.getLocation());
        player.sendMessage("Ti sei unito all'arena " + arena.getArenaName());
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> suggestions = new ArrayList<>();
        if (strings.length == 1) {
            ArenaManager arenaManager = AresonDeathSwap.instance.getArenaManager();
            suggestions.addAll(arenaManager.getArenas().keySet().stream().filter(string -> string.startsWith(strings[0])).collect(Collectors.toList()));
        }
        return suggestions;
    }
}
