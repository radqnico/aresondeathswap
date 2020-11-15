package it.areson.aresondeathswap;

import it.areson.aresondeathswap.enums.ArenaStatus;
import it.areson.aresondeathswap.utils.Countdown;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static it.areson.aresondeathswap.enums.ArenaStatus.InGame;
import static it.areson.aresondeathswap.enums.ArenaStatus.Waiting;

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
                30,
                () -> {
                    arenaStatus = InGame;
                    startGame();
                },
                () -> players.forEach(player -> aresonDeathSwap.messages.sendPlainMessage(player, "countdown-interrupted")),
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
                    if (players.size() > 0) {
                        Player winnerPlayer = players.stream().findFirst().get();
                        aresonDeathSwap.getServer().broadcastMessage(
                                aresonDeathSwap.messages.getPlainMessage("victory-message").replaceAll("%player%", winnerPlayer.getName())
                        );
                        aresonDeathSwap.teleportToLobbySpawn(winnerPlayer);
                        players.clear();
                        aresonDeathSwap.waitingPlayers.add(winnerPlayer);
                        aresonDeathSwap.assignPlayersToArenaIfPossible();
                    } else {
                        //TODO Manca player
                    }
                    arenaStatus = Waiting;
                }, 10,
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
            //TODO inconsistenza
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
        List<Location> locations = players.stream().map(Player::getLocation).collect(Collectors.toList());

        int lastIndex = locations.size() - 1;
        Location lastLocation = locations.get(lastIndex);

        for (int i = 0; i < lastIndex; i++) {
            locations.set(i + 1, locations.get(i));
        }
        locations.set(0, lastLocation);

        for (int i = 0; i < players.size(); i++) {
            players.get(i).teleport(locations.get(i));
        }
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public boolean isJoinable() {
        return arenaStatus == Waiting || arenaStatus == ArenaStatus.Starting;
    }

    public void addPlayer(Player player) {
        players.add(player);
        if (players.size() >= aresonDeathSwap.MIN_PLAYERS) {
            startPregame();
        }
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
                        interruptGame();
                    }
                    break;
            }
        }

    }

    public void startPregame() {
        if (!countdownPregame.isRunning()) {
            countdownPregame.start();
            arenaStatus = ArenaStatus.Starting;
        }
    }

    public ArenaStatus getArenaStatus() {
        return arenaStatus;
    }
}
