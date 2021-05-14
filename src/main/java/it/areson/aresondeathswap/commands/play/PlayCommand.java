package it.areson.aresondeathswap.commands.play;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.arena.Arena;
import it.areson.aresondeathswap.arena.ArenaManager;
import it.areson.aresondeathswap.arena.ArenaStatus;
import it.areson.aresondeathswap.player.DeathswapPlayer;
import it.areson.aresondeathswap.player.DeathswapPlayerManager;
import it.areson.aresondeathswap.utils.Message;
import it.areson.aresondeathswap.utils.Pair;
import it.areson.aresondeathswap.utils.PlayerUtils;
import it.areson.aresondeathswap.utils.SoundManager;
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

public class PlayCommand implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (strings.length == 1) {
                String arenaName = strings[0];
                addIfCan(player, arenaName);
            } else {
                player.sendMessage(AresonDeathSwap.instance.messages.getPlainMessage(Message.ARENA_NOT_FOUND));
            }
        } else {
            commandSender.sendMessage("Command only available by player");
        }
        return true;
    }

    private void addIfCan(Player player, String arenaName) {
        ArenaManager arenaManager = AresonDeathSwap.instance.getArenaManager();
        Optional<Arena> arenaByName = arenaManager.getArenaByName(arenaName);
        if (!arenaByName.isPresent()) {
            player.sendMessage(AresonDeathSwap.instance.messages.getPlainMessage(Message.ARENA_NOT_FOUND));
            return;
        }
        Arena arena = arenaByName.get();
        if (arena.canNewPlayerJoin()) {
            addPlayerToArena(arenaManager, arena, player);
        } else {
            player.sendMessage(AresonDeathSwap.instance.messages.getPlainMessage(Message.ARENA_CANNOT_JOIN));
        }
    }

    private void addPlayerToArena(ArenaManager arenaManager, Arena arena, Player player) {
        DeathswapPlayerManager deathswapPlayerManager = AresonDeathSwap.instance.getDeathswapPlayerManager();
        DeathswapPlayer deathswapPlayer = deathswapPlayerManager.getDeathswapPlayer(player);

        Optional<Arena> arenaOfPlayer = arenaManager.getArenaOfPlayer(deathswapPlayer);
        if (arenaOfPlayer.isPresent() && (arenaOfPlayer.get().equals(arena) || arenaOfPlayer.get().getArenaStatus().equals(ArenaStatus.IN_GAME))) {
            player.sendMessage(AresonDeathSwap.instance.messages.getPlainMessage(Message.ARENA_ALREADY_INTO));
            SoundManager.cannotJoinArena(player);
            return;
        }

        arenaManager.removePlayerFromAllArenas(deathswapPlayer);
        arena.addPlayer(deathswapPlayer, player.getLocation());

        player.sendMessage(AresonDeathSwap.instance.messages.getPlainMessage(Message.ARENA_JOIN, Pair.of("%arena%", arena.getArenaName())));

        PlayerUtils.sendShortTitle(player, Message.TITLE_JOIN_ARENA, Message.TITLE_JOIN_ARENA_SUB);
        SoundManager.joinArena(player);
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
