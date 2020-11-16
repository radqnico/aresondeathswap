package it.areson.aresondeathswap;

import it.areson.aresondeathswap.enums.ArenaStatus;
import it.areson.aresondeathswap.utils.Countdown;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static it.areson.aresondeathswap.enums.ArenaStatus.*;

public class Arena {

    private final AresonDeathSwap aresonDeathSwap;
    private final String arenaName;
    private ArrayList<Player> players;
    private final Countdown countdownPregame;
    private Countdown countdownGame;
    private ArenaStatus arenaStatus;

    public Arena(AresonDeathSwap aresonDeathSwap, String arenaName) {
        this.aresonDeathSwap = aresonDeathSwap;
        this.arenaName = arenaName;
        this.players = new ArrayList<>();
        this.arenaStatus = Waiting;

        //Countdowns
        this.countdownPregame = new Countdown(aresonDeathSwap,
                aresonDeathSwap.STARTING_TIME,
                () -> {
                    arenaStatus = InGame;
                    startGame();
                },
                () -> players.forEach(player -> aresonDeathSwap.messages.sendPlainMessage(player, "countdown-interrupted")),
                0,
                15,
                aresonDeathSwap.messages.getPlainMessage("countdown-starting-message"),
                this,
                aresonDeathSwap.messages.getPlainMessage("countdown-start-message")
        );

        this.countdownGame = new Countdown(aresonDeathSwap,
                randomTeleportTime(),
                () -> {
                    rotatePlayers();
                    countdownGame.setTime(randomTeleportTime());
                    countdownGame.start();
                },
                () -> {
                    //TP remaining players
                    World world = aresonDeathSwap.getServer().getWorld(arenaName);
                    if (world != null) {
                        world.getPlayers().forEach(aresonDeathSwap::teleportToLobbySpawn);
                    } else {
                        aresonDeathSwap.getLogger().severe("Error while getting the world while teleporting players");
                    }

                    aresonDeathSwap.reloadArenaWorld(arenaName);
                    arenaStatus = Waiting;
                },
                5,
                10,
                aresonDeathSwap.messages.getPlainMessage("countdown-swap-message"),
                this,
                aresonDeathSwap.messages.getPlainMessage("countdown-prepare-message")
        );
    }

    public void startGame() {
        World world = aresonDeathSwap.getServer().getWorld(arenaName);
        if (world != null) {
            players.forEach(player -> player.teleport(world.getSpawnLocation()));
            countdownGame.start();
            this.arenaStatus = ArenaStatus.InGame;
        } else {
            aresonDeathSwap.getLogger().severe("Cannot found arena world");
        }
    }

    private int randomTeleportTime() {
        Random random = new Random();
        return random.nextInt(aresonDeathSwap.MAX_SWAP_TIME_SECONDS - aresonDeathSwap.MIN_SWAP_TIME_SECONDS) + aresonDeathSwap.MIN_SWAP_TIME_SECONDS;
    }

    public void interruptPregame() {
        if (countdownPregame.isRunning()) {
            countdownPregame.interrupt();
        }
    }

    public void interruptGame() {
        if (countdownGame.isRunning()) {
            countdownGame.interrupt();
        }
    }

    public void rotatePlayers() {
        Collections.shuffle(players);

        List<Location> newLocations = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            if (i == (players.size() - 1)) {
                newLocations.add(players.get(0).getLocation().clone());
            } else {
                newLocations.add(players.get(i + 1).getLocation().clone());
            }
        }

        for (int i = 0; i < newLocations.size(); i++) {
            players.get(i).teleport(newLocations.get(i));
        }
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public boolean addPlayer(Player player) {
        if (arenaStatus == Waiting || arenaStatus == Starting) {
            players.forEach(
                    targetPlayer -> targetPlayer.sendMessage(
                            aresonDeathSwap.messages.getPlainMessage("player-joined-arena").replaceAll("%player%", player.getName())
                    )
            );
            players.add(player);
            if (players.size() >= aresonDeathSwap.MIN_PLAYERS) {
                startPregame();
            }
            return true;
        }
        return false;
    }

    public void removePlayer(Player player) {
        if (players.contains(player)) {
            players.remove(player);

            switch (arenaStatus) {
                case Starting:
                    if (players.size() < aresonDeathSwap.MIN_PLAYERS) {
                        interruptPregame();
                    }
                    break;
                case InGame:
                    aresonDeathSwap.teleportToLobbySpawn(player);
                    if (players.size() == 1) {
                        winGame();
                    } else {
                        players.forEach(messagePlayer -> aresonDeathSwap.messages.sendPlainMessageDelayed(messagePlayer, ""));
                    }
                    break;
            }
        }
    }

    public void winGame() {
        if (players.size() > 0) {
            Player winnerPlayer = players.stream().findFirst().get();
            aresonDeathSwap.getServer().broadcastMessage(
                    aresonDeathSwap.messages.getPlainMessage("victory-message").replaceAll("%player%", winnerPlayer.getName())
            );
            aresonDeathSwap.teleportToLobbySpawn(winnerPlayer);
            players.clear();
            arenaStatus = Ending;

            interruptGame();
        } else {
            aresonDeathSwap.getLogger().severe("Winningg game with no remaining players");
        }
    }

    public void startPregame() {
        if (!countdownPregame.isRunning()) {
            countdownPregame.start();
            arenaStatus = ArenaStatus.Starting;
        }
    }

}
