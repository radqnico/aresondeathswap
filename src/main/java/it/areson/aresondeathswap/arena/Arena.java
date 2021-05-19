package it.areson.aresondeathswap.arena;

import it.areson.aresoncore.time.countdown.Countdown;
import it.areson.aresoncore.time.countdown.CountdownManager;
import it.areson.aresoncore.time.scheduler.Scheduler;
import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.Constants;
import it.areson.aresondeathswap.arena.listeners.ArenaPregameCountdownListener;
import it.areson.aresondeathswap.arena.listeners.ArenaSwapsCountdownListener;
import it.areson.aresondeathswap.player.DeathswapPlayer;
import it.areson.aresondeathswap.utils.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Arena {

    private final HashMap<DeathswapPlayer, Location> players;
    private final AresonDeathSwap aresonDeathSwap;
    private final String arenaName;
    private final String arenaWorldName;
    private final int minPlayers;
    private final int maxRounds;
    private final HashMap<Player, Player> lastSwapCouples;
    private final ArenaPregameCountdownListener startingCountdownListener;
    private final ArenaSwapsCountdownListener swapsCountdownListener;
    private int currentRound;
    private World arenaWorld;
    private ArenaStatus arenaStatus;
    private Countdown countdownStarting;
    private Countdown countdownSwaps;

    private ArenaPlaceholders arenaPlaceholders;

    private boolean timeToKill;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<Player> winner;
    private ArrayList<Location> gameSpawns;

    private HashMap<DeathswapPlayer, Boolean> playersToRemove;
    private boolean canRemovePlayers;

    public Arena(AresonDeathSwap aresonDeathSwap, String arenaName, String arenaWorldName, int minPlayers, int maxRounds) {
        this.aresonDeathSwap = aresonDeathSwap;
        this.arenaName = arenaName;
        this.arenaWorldName = arenaWorldName;
        this.minPlayers = minPlayers;
        this.maxRounds = maxRounds;
        this.currentRound = 0;

        this.arenaStatus = ArenaStatus.CLOSED;
        this.players = new HashMap<>();

        this.lastSwapCouples = new HashMap<>();

        this.playersToRemove = new HashMap<>();
        this.canRemovePlayers = true;

        this.timeToKill = false;
        this.winner = Optional.empty();

        this.gameSpawns = new ArrayList<>();

        startingCountdownListener = new ArenaPregameCountdownListener(this);
        swapsCountdownListener = new ArenaSwapsCountdownListener(this);

        resetStartingCountdown();
        resetSwapsCountdown();

        arenaPlaceholders = new ArenaPlaceholders(this);
        arenaPlaceholders.register();
    }

    private void resetArenaData() {
        this.players.clear();
        this.lastSwapCouples.clear();
        this.playersToRemove.clear();
        this.canRemovePlayers = true;
        this.timeToKill = false;
        this.winner = Optional.empty();
        this.currentRound = 0;
    }

    public void unregisterListeners() {
        CountdownManager.getInstance().unregisterListener(startingCountdownListener);
        CountdownManager.getInstance().unregisterListener(swapsCountdownListener);
        arenaPlaceholders.unregister();
    }

    public void resetSwapsCountdown() {
        FileManager configFile = AresonDeathSwap.instance.getConfigFile();
        int max = configFile.getFileConfiguration().getInt("arena-max-swap-seconds");
        int min = configFile.getFileConfiguration().getInt("arena-min-swap-seconds");
        int random = (int) (Math.random() * (max - min)) + min;
        countdownSwaps = new Countdown(arenaName + Constants.COUNTDOWN_SWAP_SUFFIX, random, 1000, 10);
    }

    public void startSwapsCountdown() {
        CountdownManager.getInstance().startCountdown(countdownSwaps);
    }

    public void interruptSwaps() {
        CountdownManager.getInstance().interruptCountdown(arenaName + Constants.COUNTDOWN_SWAP_SUFFIX, false);
    }

    public void resetStartingCountdown() {
        countdownStarting = new Countdown(arenaName + Constants.COUNTDOWN_PREGAME_SUFFIX, 31, 5, 10);
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

    public int getMinPlayers() {
        return minPlayers;
    }

    public void open() {
        this.arenaStatus = ArenaStatus.OPEN;
    }

    public void addPlayer(DeathswapPlayer deathswapPlayer, Location previousLocation) {
        players.putIfAbsent(deathswapPlayer, previousLocation);

        if (players.size() > gameSpawns.size()) {
            Location randomLocationInRadius = ArenaUtils.getRandomLocationInRadius(arenaWorld, 1000);
            gameSpawns.add(randomLocationInRadius);
        }

        openToStartingIfMinPlayersReached();

        sendMessageToArenaPlayers(aresonDeathSwap.messages.getPlainMessage(
                Message.ARENA_JOIN_OTHERS,
                Pair.of("%player%", deathswapPlayer.getNickName())
        ));
    }

    public void loadArenaWorld() {
        arenaWorld = aresonDeathSwap.getServer().createWorld(new WorldCreator(arenaWorldName));
        if (arenaWorld != null) {
            arenaWorld.setAutoSave(false);
            arenaWorld.getWorldBorder().setCenter(arenaWorld.getSpawnLocation());
            arenaWorld.getWorldBorder().setSize(2000);
        }
    }

    public void unloadArenaWorld() {
        if (aresonDeathSwap.getServer().unloadWorld(arenaWorld, false)) {
            aresonDeathSwap.getLogger().info("Unloaded arena world " + arenaName);
        } else {
            aresonDeathSwap.getLogger().info("CANNOT unload arena world " + arenaName);
        }
    }

    private void returnPlayerToPreviousLocation(DeathswapPlayer deathswapPlayer) {
        deathswapPlayer.getActualPlayer().ifPresent(player -> {
            PlayerUtils.resetPlayerStatus(player);
            player.teleport(players.get(deathswapPlayer));
        });
    }

    public void removePlayer(DeathswapPlayer deathswapPlayer, boolean checkStatusOrWin) {
        if (!canRemovePlayers) {
            playersToRemove.put(deathswapPlayer, checkStatusOrWin);
            return;
        }

        if (arenaStatus.equals(ArenaStatus.IN_GAME) || arenaStatus.equals(ArenaStatus.CLOSED)) {
            returnPlayerToPreviousLocation(deathswapPlayer);
            aresonDeathSwap.getDeathswapPlayerManager().saveDeathswapPlayer(deathswapPlayer);
        }

        Optional<Player> actualPlayerOptional = deathswapPlayer.getActualPlayer();
        players.remove(deathswapPlayer);

        if (checkStatusOrWin) {
            startingToOpenIfNotMinPlayersReach();
            inGameWinIfLastOne();
        }

        if (arenaStatus.equals(ArenaStatus.IN_GAME) || arenaStatus.equals(ArenaStatus.CLOSED)) {
            actualPlayerOptional.ifPresent(player -> arenaWorld.strikeLightningEffect(player.getLocation()));
            deathswapPlayer.setGamesPlayed(deathswapPlayer.getGamesPlayed() + 1);
            deathswapPlayer.setDeathCount(deathswapPlayer.getDeathCount() + 1);

            actualPlayerOptional.ifPresent(player -> {
                if (!winner.isPresent() || !winner.get().equals(player)) {
                    if (timeToKill) {
                        Player killer = lastSwapCouples.get(player);
                        sendMessageToArenaPlayers(aresonDeathSwap.messages.getPlainMessage(
                                Message.GAME_KILL,
                                Pair.of("%killer%", killer.getName()),
                                Pair.of("%player%", deathswapPlayer.getNickName())
                        ));
                        DeathswapPlayer killerDSPlayer = aresonDeathSwap.getDeathswapPlayerManager().getDeathswapPlayer(killer);
                        killerDSPlayer.setKillCount(killerDSPlayer.getKillCount() + 1);
                    }

                    sendMessageToArenaPlayers(aresonDeathSwap.messages.getPlainMessage(
                            Message.GAME_PLAYERS_REMAINING,
                            Pair.of("%number%", players.size() + ""),
                            Pair.of("%players%", players.keySet().parallelStream().map(DeathswapPlayer::getNickName).collect(Collectors.joining(" ")))
                    ));

                    player.sendMessage(aresonDeathSwap.messages.getPlainMessage(Message.GAME_PLAYER_DEAD));
                    PlayerUtils.sendLongTitle(player, Message.TITLE_LOSE, Message.TITLE_LOSE_SUB);
                    SoundManager.loser(player);
                }
            });
        }
    }

    private void inGameWinIfLastOne() {
        if (arenaStatus.equals(ArenaStatus.IN_GAME) && players.size() == 1) {
            interruptSwaps();
            winGame();
        }
    }

    private void winGame() {
        sendTitleToArenaPlayers(Message.TITLE_WIN, Message.TITLE_WIN_SUB);

        Optional<DeathswapPlayer> lastPlayerInArena = getLastPlayerInArena();
        lastPlayerInArena.ifPresent(deathswapPlayer -> {
            deathswapPlayer.setDeathCount(deathswapPlayer.getDeathCount() - 1);
        });
        lastPlayerInArena.flatMap(DeathswapPlayer::getActualPlayer).ifPresent(playerWinner -> {
            PlayerUtils.playerDeadStatus(playerWinner);
            winner = Optional.of(playerWinner);
            SoundManager.winner(playerWinner);
            aresonDeathSwap.getServer().getOnlinePlayers().forEach(player -> player.sendMessage(AresonDeathSwap.instance.messages.getPlainMessage(Message.WIN_BROADCAST, Pair.of("%player%", playerWinner.getName()))));
        });

        Scheduler.getInstance().scheduleTask(arenaName + "_winGame", LocalDateTime.now().plus(6, ChronoUnit.SECONDS), () -> {
            removeAllPlayersFromArena();
            resetArena();
        });
    }

    public void removeAllPlayersFromArena() {
        for (Map.Entry<DeathswapPlayer, Location> entry : players.entrySet()) {
            removePlayer(entry.getKey(), false);
        }
        players.clear();
    }

    public void removeAllPendingPlayers() {
        for (Map.Entry<DeathswapPlayer, Boolean> entry : playersToRemove.entrySet()) {
            removePlayer(entry.getKey(), entry.getValue());
        }
        playersToRemove.clear();
    }

    public boolean isMinPlayerReached() {
        return players.size() >= minPlayers;
    }

    public void openToStartingIfMinPlayersReached() {
        if (arenaStatus.equals(ArenaStatus.OPEN) && isMinPlayerReached()) {
            arenaStatus = ArenaStatus.STARTING;
            startStartingCountdown();

            for (Player player : players.keySet().stream().map(DeathswapPlayer::getActualPlayer).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList())) {
                SoundManager.startingGame(player);
            }

            sendMessageToArenaPlayers(aresonDeathSwap.messages.getPlainMessage(
                    Message.STARTING_CD_STARTED,
                    Pair.of("%seconds%", countdownStarting.getCountdownSeconds() + "")
            ));
        }
    }

    public void startingToOpenIfNotMinPlayersReach() {
        if (arenaStatus.equals(ArenaStatus.STARTING) && !isMinPlayerReached()) {
            arenaStatus = ArenaStatus.OPEN;
            CountdownManager.getInstance().interruptCountdown(countdownStarting.getName(), false);
            resetStartingCountdown();

            for (Player player : players.keySet().stream().map(DeathswapPlayer::getActualPlayer).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList())) {
                SoundManager.startingGameInterrupted(player);
            }

            sendMessageToArenaPlayers(aresonDeathSwap.messages.getPlainMessage(
                    Message.STARTING_CD_INTERRUPTED
            ));
        }
    }

    public boolean canNewPlayerJoin() {
        return arenaStatus.equals(ArenaStatus.OPEN) || arenaStatus.equals(ArenaStatus.STARTING);
    }

    public void startGame() {
        if (arenaStatus.equals(ArenaStatus.STARTING)) {
            arenaWorld.setTime((int) (Math.random() * 24000));
            arenaStatus = ArenaStatus.IN_GAME;
            sendPlayersIntoArenaWorld();
            startSwapsCountdown();

            sendMessageToArenaPlayers(AresonDeathSwap.instance.messages.getPlainMessage(Message.GAME_STARTED));
        }
    }

    public void sendMessageToArenaPlayers(String message) {
        for (DeathswapPlayer deathswapPlayer : players.keySet()) {
            Optional<Player> actualPlayer = deathswapPlayer.getActualPlayer();
            actualPlayer.ifPresent(player -> player.sendMessage(message));
        }
        AresonDeathSwap.instance.getLogger().info("ARENA: " + arenaName + " | " + message);
    }

    public void sendTitleToArenaPlayers(String titleKey, String subTitleKey) {
        for (DeathswapPlayer deathswapPlayer : players.keySet()) {
            Optional<Player> actualPlayer = deathswapPlayer.getActualPlayer();
            actualPlayer.ifPresent(player -> PlayerUtils.sendShortTitle(player, titleKey, subTitleKey));
        }
    }

    public void swapPlayers() {
        canRemovePlayers = false;

        aresonDeathSwap.getLogger().info("Rotating " + players.size() + " players in arena " + arenaName);

        List<Player> players = this.players.keySet().stream().map(DeathswapPlayer::getActualPlayer).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());

        List<Player> shuffledPlayers = new ArrayList<>(players);

        for (int i = shuffledPlayers.size() - 1; i > 0; i--) {
            Player tmp = shuffledPlayers.get(i);
            int randomIndex = (int) (Math.random() * i);
            shuffledPlayers.set(i, shuffledPlayers.get(randomIndex));
            shuffledPlayers.set(randomIndex, tmp);
        }

        lastSwapCouples.clear();
        HashMap<Player, Location> destinations = new HashMap<>();

        for (int i = 0; i < players.size(); i++) {
            lastSwapCouples.put(players.get(i), shuffledPlayers.get(i));
            destinations.put(players.get(i), shuffledPlayers.get(i).getLocation().clone());
        }

        List<CompletableFuture<Boolean>> teleportsResults = new ArrayList<>();

        for (Map.Entry<Player, Location> entry : destinations.entrySet()) {
            Player playerToTeleport = entry.getKey();
            float yaw = playerToTeleport.getLocation().getYaw();
            float pitch = playerToTeleport.getLocation().getPitch();
            Location destination = entry.getValue().clone();
            destination.setYaw(yaw);
            destination.setPitch(pitch);
            teleportsResults.add(playerToTeleport.teleportAsync(destination).whenComplete((aBoolean, throwable) -> {
                PlayerUtils.sendShortTitle(playerToTeleport, Message.TITLE_SWAP, Message.TITLE_SWAP_SUB);
                SoundManager.teleport(playerToTeleport);

                if (Math.random() < 0.5) {
                    spawnChestNearPlayer(playerToTeleport);
                }
            }));
        }

        CompletableFuture.allOf(teleportsResults.toArray(new CompletableFuture[0])).whenComplete((unused, throwable) -> {
            canRemovePlayers = true;
            removeAllPendingPlayers();
            timeToKill = true;

            currentRound++;
            if (getRemainingRounds() == 0) {
                witherPlayers(players);
            }

            Scheduler.getInstance().scheduleTask(arenaName + "_timeToKill", LocalDateTime.now().plus(10, ChronoUnit.SECONDS), () -> {
                timeToKill = false;
            });
        });

    }

    public void witherPlayers(List<Player> players) {
        for (Player player : players) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, Integer.MAX_VALUE, 3));
        }
    }

    public int getRemainingRounds() {
        return Math.max(maxRounds - currentRound, 0);
    }

    private void sendPlayersIntoArenaWorld() {
        List<Player> players = this.players.keySet().parallelStream().map(DeathswapPlayer::getActualPlayer).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());

        for (Player player : players) {
            player.teleportAsync(gameSpawns.remove(0)).whenComplete((aBoolean, throwable) -> {
                player.getLocation().clone().add(0, -1, 0).getBlock().setType(Material.GLASS);
                PlayerUtils.resetPlayerStatus(player);
                player.getInventory().clear();
                PlayerUtils.giveInitialKit(player);

                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 200, 1));

                SoundManager.gameStarted(player);

                spawnChestNearPlayer(player);
            });
        }

        gameSpawns.clear();
    }

    public void spawnChestNearPlayer(Player player) {
        aresonDeathSwap.loots.placeNewChestNear(player);
        player.sendMessage(aresonDeathSwap.messages.getPlainMessage(Message.GAME_CHEST_SPAWNED));
        SoundManager.chestAppear(player);
    }

    private void resetArena() {
        close();
        resetArenaData();
        unloadArenaWorld();
        Scheduler.getInstance().scheduleTask(arenaName + "_reloadWorld", LocalDateTime.now().plus(5, ChronoUnit.SECONDS), () -> {
            loadArenaWorld();
            open();
        });
        resetSwapsCountdown();
        resetStartingCountdown();
    }

    private void close() {
        arenaStatus = ArenaStatus.CLOSED;
    }

    private Optional<DeathswapPlayer> getLastPlayerInArena() {
        return players.keySet().stream().findFirst();
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

    public int getMaxRounds() {
        return maxRounds;
    }
}
