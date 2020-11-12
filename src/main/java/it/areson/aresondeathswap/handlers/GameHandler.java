package it.areson.aresondeathswap.handlers;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.utils.Countdown;
import it.areson.aresondeathswap.utils.PlayerHolder;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Optional;
import java.util.Random;

public class GameHandler {

    private final AresonDeathSwap aresonDeathSwap;
    private final PlayerHolder players;
    private Countdown gameCountdown;
    private final String arenaName;


    public GameHandler(AresonDeathSwap plugin, String arenaName) {
        aresonDeathSwap = plugin;
        this.players = new PlayerHolder(plugin);
        this.arenaName = arenaName;
    }

    public void startGame() {
        World world = aresonDeathSwap.getServer().getWorld(arenaName);
        if (world != null) {
            HashSet<Player> players = aresonDeathSwap.arenasPlayers.get(arenaName);
            if (players != null) {
                players.forEach(player -> player.teleport(world.getSpawnLocation()));
            } else {
                //TODO Incosistenza
            }
        } else {
            //TODO inconsistenza
        }


        gameCountdown = new Countdown(aresonDeathSwap, randomTeleportTime(), () -> {
            players.playerRotate();
            gameCountdown.resetCountdown(randomTeleportTime());
            gameCountdown.start();
        }, () -> {
            HashSet<Player> players = aresonDeathSwap.arenasPlayers.get(arenaName);
            if (players != null) {
                if (players.size() > 0) {
                    Player winnerPlayer = players.stream().findFirst().get();
                    aresonDeathSwap.getServer().broadcastMessage(
                            aresonDeathSwap.messages.getPlainMessage("victory-message").replaceAll("%player%", winnerPlayer.getName())
                    );
                    aresonDeathSwap.teleportToLobbySpawn(winnerPlayer);
                    aresonDeathSwap.arenasPlayers.put(arenaName, new HashSet<>());
                    aresonDeathSwap.joinableArenas.put(arenaName, true);
                } else {
                    //TODO Manca player
                }
            } else {
                //TODO inconsistenza
            }
        }, 10, aresonDeathSwap.messages.getPlainMessage("countdown-swap-message"),
                arenaName, aresonDeathSwap.messages.getPlainMessage("countdown-prepare-message"));

        gameCountdown.start();
    }

    private int randomTeleportTime() {
        Random random = new Random();
        return random.nextInt(aresonDeathSwap.MAX_SWAP_TIME_SECONDS - aresonDeathSwap.MIN_SWAP_TIME_SECONDS) + aresonDeathSwap.MIN_SWAP_TIME_SECONDS;
    }

    public boolean isRunning() {
        return gameCountdown.isRunning();
    }

    public void stop() {
        if (isRunning()) {
            gameCountdown.interrupt();
        }
    }
}
