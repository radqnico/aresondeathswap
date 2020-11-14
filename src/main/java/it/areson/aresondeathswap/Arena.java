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

public class Arena {

    private final AresonDeathSwap aresonDeathSwap;
    private String arenaName;
    private boolean isJoinable;
    private ArrayList<Player> players;
    private Countdown countdownPregame;
    private Countdown countdownGame;
    private ArenaStatus arenaStatus;

    public Arena(AresonDeathSwap aresonDeathSwap, String arenaName) {
        this.aresonDeathSwap = aresonDeathSwap;
        this.arenaName = arenaName;
        this.isJoinable = true;
        this.players = new ArrayList<>();
        this.arenaStatus = ArenaStatus.Waiting;
        //Countdowns
        this.countdownPregame = new Countdown(aresonDeathSwap,
                30,
                () -> {
                    isJoinable = false;
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
                    } else {
                        //TODO Manca player
                    }
                    isJoinable = true;
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

    public void stopPregame() {
        if (countdownPregame.isRunning()) {
            countdownPregame.interrupt();
        }
    }

    public void stopGame() {
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
        return isJoinable;
    }

    public void addPlayer(Player player){
        players.add(player);
    }

    public void startPregame(){
        if(!countdownPregame.isRunning()) {
            countdownPregame.start();
            this.arenaStatus = ArenaStatus.Starting;
        }
    }

    public ArenaStatus getArenaStatus() {
        return arenaStatus;
    }
}
