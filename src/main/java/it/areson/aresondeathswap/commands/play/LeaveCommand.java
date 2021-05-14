package it.areson.aresondeathswap.commands.play;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.arena.ArenaManager;
import it.areson.aresondeathswap.player.DeathswapPlayer;
import it.areson.aresondeathswap.player.DeathswapPlayerManager;
import it.areson.aresondeathswap.utils.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LeaveCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            removePlayerFromArenas(player);
            player.sendMessage(AresonDeathSwap.instance.messages.getPlainMessage(Message.GAME_LEAVE));
        } else {
            commandSender.sendMessage("Command only available by player");
        }
        return true;
    }

    private void removePlayerFromArenas(Player player) {
        DeathswapPlayerManager deathswapPlayerManager = AresonDeathSwap.instance.getDeathswapPlayerManager();
        DeathswapPlayer deathswapPlayer = deathswapPlayerManager.getDeathswapPlayer(player);
        AresonDeathSwap.instance.getArenaManager().removePlayerFromAllArenas(deathswapPlayer);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> suggestions = new ArrayList<>();
        if (strings.length == 1) {
            ArenaManager arenaManager = AresonDeathSwap.instance.getArenaManager();
            suggestions.addAll(arenaManager.getArenas().keySet());
        }
        return suggestions;
    }
}
