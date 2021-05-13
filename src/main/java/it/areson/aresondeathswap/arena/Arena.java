package it.areson.aresondeathswap.arena;

import it.areson.aresoncore.loadbalancer.LoadBalancer;
import it.areson.aresoncore.time.countdown.Countdown;
import it.areson.aresoncore.time.countdown.CountdownManager;
import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.Constants;
import it.areson.aresondeathswap.arena.listeners.ArenaPregameCountdownListener;
import it.areson.aresondeathswap.arena.listeners.ArenaSwapsCountdownListener;
import it.areson.aresondeathswap.player.DeathswapPlayer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class Arena {

    private final HashMap<DeathswapPlayer, Location> players;
    private final AresonDeathSwap aresonDeathSwap;
    private final String arenaName;
    private final String arenaWorldName;
    private final int minPlayers;
    private World arenaWorld;
    private ArenaStatus arenaStatus;
    private Countdown countdownStarting;
    private Countdown countdownSwaps;
    private HashMap<Player, Player> lastSwapCouples;
    private ArenaPregameCountdownListener pregameCountdownListener;
    private ArenaSwapsCountdownListener swapsCountdownListener;

    public Arena(AresonDeathSwap aresonDeathSwap, String arenaName, String arenaWorldName, int minPlayers) {
        this.aresonDeathSwap = aresonDeathSwap;
        this.arenaName = arenaName;
        this.arenaWorldName = arenaWorldName;
        this.minPlayers = minPlayers;

        this.arenaStatus = ArenaStatus.CLOSED;
        this.players = new HashMap<>();

        this.lastSwapCouples = new HashMap<>();

        pregameCountdownListener = new ArenaPregameCountdownListener(this);
        swapsCountdownListener = new ArenaSwapsCountdownListener(this);

        resetStartingCountdown();
        resetSwapsCountdown();
    }

    public void unregisterListeners(){
        CountdownManager.getInstance().unregisterListener(pregameCountdownListener);
        CountdownManager.getInstance().unregisterListener(swapsCountdownListener);
    }

    public void resetSwapsCountdown() {
        int random = (int) (Math.random() * 10) + 5;
        countdownSwaps = new Countdown(arenaName + Constants.COUNTDOWN_SWAP_SUFFIX, random, 500, 10);
    }

    public void startSwapsCountdown() {
        CountdownManager.getInstance().startCountdown(countdownSwaps);
    }

    public void interruptSwaps() {
        CountdownManager.getInstance().interruptCountdown(arenaName + Constants.COUNTDOWN_SWAP_SUFFIX, false);
    }

    public void resetStartingCountdown() {
        countdownStarting = new Countdown(arenaName + Constants.COUNTDOWN_PREGAME_SUFFIX, 16, 5, 10);
    }

    private void startStartingCountdown() {
        CountdownManager.getInstance().startCountdown(countdownStarting);
    }

    public HashMap<DeathswapPlayer, Location> getPlayers() {
        return players;
    }

    public String getArenaName() {
        return arenaName;
    }

    public World getArenaWorld() {
        return arenaWorld;
    }

    public ArenaStatus getArenaStatus() {
        return arenaStatus;
    }

    public void open() {
        this.arenaStatus = ArenaStatus.OPEN;
    }

    public void addPlayer(DeathswapPlayer deathswapPlayer, Location previousLocation) {
        players.putIfAbsent(deathswapPlayer, previousLocation);
        openToStartingIfMinPlayersReached();
    }

    public void loadArenaWorld() {
        arenaWorld = aresonDeathSwap.getServer().createWorld(new WorldCreator(arenaWorldName));
        if (arenaWorld != null) {
            arenaWorld.setAutoSave(false);
        }
    }

    public void unloadArenaWorld() {
        if (aresonDeathSwap.getServer().unloadWorld(arenaWorld, false)) {
            aresonDeathSwap.getLogger().info("Unloaded arena world " + arenaName);
        } else {
            aresonDeathSwap.getLogger().info("CANNOT unload arena world " + arenaName);
        }
    }

    private void returnPlayerToPreviousLocation(DeathswapPlayer player) {
        player.getActualPlayer().ifPresent(player1 -> player1.teleport(players.get(player)));
    }

    public void removePlayer(DeathswapPlayer deathswapPlayer) {
        if (arenaStatus.equals(ArenaStatus.IN_GAME)) {
            returnPlayerToPreviousLocation(deathswapPlayer);
        }
        players.remove(deathswapPlayer);
        startingToOpenIfNotMinPlayersReach();
    }

    public void removeAllPlayersFromArena() {
        for (Map.Entry<DeathswapPlayer, Location> entry : players.entrySet()) {
            removePlayer(entry.getKey());
        }
        players.clear();
    }

    public boolean isMinPlayerReached() {
        return players.size() >= minPlayers;
    }

    public void openToStartingIfMinPlayersReached() {
        if (arenaStatus.equals(ArenaStatus.OPEN) && isMinPlayerReached()) {
            arenaStatus = ArenaStatus.STARTING;
            startStartingCountdown();
        }
    }

    public void startingToOpenIfNotMinPlayersReach() {
        if (arenaStatus.equals(ArenaStatus.STARTING) && !isMinPlayerReached()) {
            arenaStatus = ArenaStatus.OPEN;
            CountdownManager.getInstance().interruptCountdown(countdownStarting.getName(), false);
            resetStartingCountdown();
            sendMessageToArenaPlayers("Il gioco non può iniziare per mancanza di giocatori!");
        }
    }

    public boolean canNewPlayerJoin() {
        return arenaStatus.equals(ArenaStatus.OPEN) || arenaStatus.equals(ArenaStatus.STARTING);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Arena)) return false;
        Arena arena = (Arena) o;
        return Objects.equals(arenaName, arena.arenaName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arenaName);
    }

    public void startGame() {
        if (arenaStatus.equals(ArenaStatus.STARTING)) {
            arenaStatus = ArenaStatus.IN_GAME;
            startSwapsCountdown();
        }
    }

    public void sendMessageToArenaPlayers(String message) {
        for (DeathswapPlayer deathswapPlayer : players.keySet()) {
            Optional<Player> actualPlayer = deathswapPlayer.getActualPlayer();
            actualPlayer.ifPresent(player -> player.sendMessage(message));
        }
        AresonDeathSwap.instance.getLogger().info("ARENA: " + arenaName + " | " + message);
    }

    public void swapPlayers() {
        aresonDeathSwap.getLogger().info("Rotating " + players.size() + " players in arena " + arenaName);

        List<Player> players = this.players.keySet().stream().map(DeathswapPlayer::getActualPlayer).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());

        List<Player> shuffledPlayers = Arrays.asList(new Player[players.size()]);

        ArrayList<Integer> indexes = new ArrayList<>();

        for (int i = 0; i < players.size(); i++) {
            indexes.clear();
            for (int j = 0; j < players.size(); j++) {
                if ((i != j && Objects.isNull(shuffledPlayers.get(j)))) {
                    indexes.add(j);
                }
            }
            int randomIndex = (int) (Math.random() * indexes.size());
            shuffledPlayers.set(indexes.get(randomIndex), players.get(i));
        }

        lastSwapCouples.clear();
        HashMap<Player, Location> destinations = new HashMap<>();

        for (int i = 0; i < players.size(); i++) {
            lastSwapCouples.put(players.get(i), shuffledPlayers.get(i));
            destinations.put(players.get(i), shuffledPlayers.get(i).getLocation().clone());
        }

        for (Map.Entry<Player, Location> entry : destinations.entrySet()) {
            Player playerToTeleport = entry.getKey();
            Location locationPre = playerToTeleport.getLocation().clone();

            playerToTeleport.teleport(entry.getValue());
            playerToTeleport.getLocation().setYaw(locationPre.getYaw());
            playerToTeleport.getLocation().setPitch(locationPre.getPitch());
        }

        String result = lastSwapCouples.entrySet().stream()
                .map(e -> e.getKey() + " -> " + e.getValue())
                .collect(Collectors.joining("; "));

        aresonDeathSwap.getLogger().info(result);

    }
}
