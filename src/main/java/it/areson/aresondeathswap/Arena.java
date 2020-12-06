package it.areson.aresondeathswap;

import it.areson.aresondeathswap.enums.ArenaStatus;
import it.areson.aresondeathswap.utils.ArenaPlaceholders;
import it.areson.aresondeathswap.utils.Countdown;
import it.areson.aresondeathswap.utils.DelayedRepeatingTask;
import it.areson.aresondeathswap.utils.StringPair;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static it.areson.aresondeathswap.enums.ArenaStatus.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Arena {

    private final AresonDeathSwap aresonDeathSwap;
    private final String arenaName;
    private final Countdown countdownPregame;
    private final ArrayList<Player> players;
    private DelayedRepeatingTask countdownGame;
    private ArenaStatus arenaStatus;

    private final ArenaPlaceholders placeholders;

    private final ArrayList<Location> spawns;

    private final ArrayList<Player> tpFroms;
    private final ArrayList<Player> tpTos;

    private Optional<LocalDateTime> lastSwapTime;

    private int roundCounter;

    public Arena(AresonDeathSwap aresonDeathSwap, String arenaName) {
        this.aresonDeathSwap = aresonDeathSwap;
        this.arenaName = arenaName;
        this.players = new ArrayList<>();
        this.arenaStatus = Waiting;
        lastSwapTime = Optional.empty();
        tpFroms = new ArrayList<>();
        tpTos = new ArrayList<>();
        roundCounter = 0;
        spawns = new ArrayList<>();
        placeholders = new ArenaPlaceholders(this.arenaStatus, this.arenaName, this.players);
        placeholders.register();
        //Countdowns
        this.countdownPregame = new Countdown(aresonDeathSwap,
                aresonDeathSwap.STARTING_TIME,
                () -> {
                    arenaStatus = InGame;
                    placeholders.setArenaStatus(InGame);
                    startGame();
                },
                () -> {
                    arenaStatus = Waiting;
                    placeholders.setArenaStatus(Waiting);
                    ArrayList<Player> copiedPlayers = new ArrayList<>(players);
                    copiedPlayers.forEach(player -> aresonDeathSwap.messages.sendPlainMessage(player, "countdown-interrupted"));
                },
                0,
                15,
                aresonDeathSwap.messages.getPlainMessage("countdown-starting-message"),
                this,
                aresonDeathSwap.messages.getPlainMessage("countdown-start-message")
        );

        this.countdownGame = new DelayedRepeatingTask(
                aresonDeathSwap,
                randomTeleportTime(),
                () -> {
                    try {
                        rotatePlayers();
                        int swapTime = randomTeleportTime();
                        countdownGame.setEverySeconds(swapTime);
                        aresonDeathSwap.getLogger().info("Started new countdownGame in arena " + arenaName + " with " + swapTime + " seconds");
                        roundCounter++;
                        lastSwapTime = Optional.of(LocalDateTime.now());
                        if (roundCounter > aresonDeathSwap.MAX_ROUNDS) {
                            witherPlayers();
                            placeholders.setRoundsRemainingString("Round finali");
                        } else {
                            ArrayList<Player> copiedPlayers = new ArrayList<>(players);
                            copiedPlayers.forEach(player -> aresonDeathSwap.messages.sendPlainMessage(
                                    player,
                                    "rounds-remaining",
                                    StringPair.of("%remaining%", (aresonDeathSwap.MAX_ROUNDS - roundCounter) + "")
                            ));
                            placeholders.setRoundsRemainingString(roundCounter + "/" + aresonDeathSwap.MAX_ROUNDS);
                        }
                    } catch (Exception e) {
                        System.out.println("ECCOLO !!!!");
                        e.printStackTrace(System.out);
                    }
                },
                Optional.of(aresonDeathSwap.messages.getPlainMessage("countdown-swap-message")),
                players
        );
    }

    public Optional<LocalDateTime> getLastSwapTime() {
        return lastSwapTime;
    }

    public void rotatePlayers() {
        aresonDeathSwap.getLogger().info("Rotating " + players.size() + " players in arena " + arenaName);
        tpFroms.clear();
        tpTos.clear();
        ArrayList<Location> playerLocation = new ArrayList<>();
        HashMap<Player, Location> playerDestination = new HashMap<>();

        ArrayList<Player> copiedPlayers = new ArrayList<>(players);
        Collections.shuffle(copiedPlayers);
        copiedPlayers.forEach(player -> playerLocation.add(player.getLocation().clone()));

        for (int i = 0; i < copiedPlayers.size(); i++) {
            if (i == (copiedPlayers.size() - 1)) {
                playerDestination.put(copiedPlayers.get(i), playerLocation.get(0));
                tpTos.add(copiedPlayers.get(0));
            } else {
                playerDestination.put(copiedPlayers.get(i), playerLocation.get(i + 1));
                tpTos.add(copiedPlayers.get(i + 1));
            }
        }

        playerDestination.forEach((player, destination) -> {
            tpFroms.add(player);
            player.teleportAsync(destination).whenComplete((input, exception) -> {
                if(input) {
                    if(!player.getWorld().getName().equals(aresonDeathSwap.MAIN_WORLD_NAME)) {
                        if (Math.random() < 0.5) {
                            aresonDeathSwap.loot.placeNewChestNear(player);
                            aresonDeathSwap.messages.sendPlainMessage(player, "chest-spawned");
                            aresonDeathSwap.sounds.chestAppear(player);
                        }
                        aresonDeathSwap.sounds.teleport(player);
                        aresonDeathSwap.titles.sendShortTitle(player, "swap");
                    }
                } else {
                    removePlayer(player);
                }
            });
        });
    }

    private void witherPlayers() {
        ArrayList<Player> copiedPlayers = new ArrayList<>(players);
        copiedPlayers.forEach(player -> player.addPotionEffect(new PotionEffect(
                PotionEffectType.WITHER,
                Integer.MAX_VALUE,
                3,
                false,
                false,
                false
        )));
    }

    private Location getRandomLocationAroundSpawn(World world) {
        Location spawnLocation = world.getSpawnLocation();
        Random random = new Random();
        int dx = (random.nextBoolean() ? 1 : -1) * random.nextInt(3000);
        int dz = (random.nextBoolean() ? 1 : -1) * random.nextInt(3000);
        Location clone = spawnLocation.clone();
        Location add = clone.add(dx, 0, dz);
        int highestBlockYAt = world.getHighestBlockYAt(add);
        add.setY(highestBlockYAt);
        return add;
    }

    //TODO min max dinamici

    public void startGame() {
        roundCounter = 0;
        World world = aresonDeathSwap.getServer().getWorld(arenaName);
        if (world != null) {
            world.setTime((int) (Math.random() * 24000));
            ArrayList<Player> copiedPlayers = new ArrayList<>(players);

            copiedPlayers.forEach(player -> {
                try {
                    Location removedSpawn = spawns.remove(0);
                    aresonDeathSwap.effects.joinedArena(player);
                    player.teleportAsync(removedSpawn).whenComplete((result, exception) -> {
                        if (result) {
                            aresonDeathSwap.loot.placeNewChestNear(player);
                            aresonDeathSwap.messages.sendPlainMessage(player, "chest-spawned");
                            aresonDeathSwap.sounds.openChest(player.getLocation());
                            aresonDeathSwap.sounds.gameStarted(player);
                            aresonDeathSwap.titles.sendLongTitle(player, "start");
                            placeholders.setRoundsRemainingString(roundCounter + "/" + aresonDeathSwap.MAX_ROUNDS);
                            aresonDeathSwap.eventCall.callPlayerStartGame(player);
                        } else {
                            aresonDeathSwap.messages.sendPlainMessage(player, "teleport-fail");
                            aresonDeathSwap.removePlayerFromArenas(player);
                        }
                    });
                    player.getInventory().clear();
                } catch (IndexOutOfBoundsException e) {
                    player.teleportAsync(getRandomLocationAroundSpawn(world));
                }

            });
            countdownGame.startRepeating();
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
            ArrayList<Player> copiedPlayers = new ArrayList<>(players);
            copiedPlayers.forEach(player -> aresonDeathSwap.sounds.startingGameInterrupted(player));
        }
    }

    public void interruptGame() {
        World world = aresonDeathSwap.getServer().getWorld(arenaName);
        List<CompletableFuture<Boolean>> teleports = new ArrayList<>();
        if (world != null) {
            for (Player player : world.getPlayers()) {
                teleports.add(aresonDeathSwap.teleportToLobbySpawn(player));
            }
            spawns.clear();
        } else {
            aresonDeathSwap.getLogger().severe("Error while getting the world while teleporting players");
        }
        CompletableFuture<List<Boolean>> listCompletableFuture = CompletableFuture.allOf(teleports.toArray(new CompletableFuture[0]))
                .thenApply(ignored -> teleports.stream().map(CompletableFuture::join).collect(Collectors.toList()));

        listCompletableFuture.whenComplete((booleans, throwable) -> {

            if (!booleans.contains(false)) {
                countdownGame.stopRepeating();
                aresonDeathSwap.loot.removeChestOfWorld(arenaName);

                aresonDeathSwap.getServer().getScheduler().scheduleSyncDelayedTask(aresonDeathSwap, () -> {
                    if (aresonDeathSwap.getServer().unloadWorld(arenaName, false)) {
                        aresonDeathSwap.getServer().getScheduler().scheduleSyncDelayedTask(aresonDeathSwap, () -> {
                            if (aresonDeathSwap.loadArenaWorld(arenaName)) {
                                arenaStatus = Waiting;
                                placeholders.setArenaStatus(Waiting);
                                placeholders.setRoundsRemainingString("Non in gioco");
                                lastSwapTime = Optional.empty();
                                aresonDeathSwap.getLogger().info("Game on '" + arenaName + "' interrupted");
                            } else {
                                aresonDeathSwap.getLogger().severe("Error while loading world " + arenaName);
                            }
                        }, 10 * 20);

                        aresonDeathSwap.getLogger().info("World " + arenaName + " unloaded. Tasked the load");
                    } else {
                        aresonDeathSwap.getLogger().severe("Error while unloading world " + arenaName);
                    }
                }, 10 * 20);
            } else {
                aresonDeathSwap.getLogger().severe("Error while teleporting to main world from " + arenaName);
            }
        });
    }


    public ArrayList<Player> getPlayers() {
        return players;
    }

    public boolean addPlayer(Player player) {
        if (arenaStatus == Waiting || arenaStatus == Starting) {
            ArrayList<Player> copiedPlayers = new ArrayList<>(players);
            copiedPlayers.forEach(
                    targetPlayer -> targetPlayer.sendMessage(
                            aresonDeathSwap.messages.getPlainMessage("player-joined-arena").replaceAll("%player%", player.getName())
                    )
            );
            players.add(player);
            // Async spawn generation
            aresonDeathSwap.getServer().getScheduler().runTaskAsynchronously(aresonDeathSwap, () -> {
                World world = aresonDeathSwap.getServer().getWorld(arenaName);
                if (world != null) {
                    spawns.add(getRandomLocationAroundSpawn(world));
                }
            });
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
                    spawns.remove(0);
                    if (players.size() < aresonDeathSwap.MIN_PLAYERS) {
                        interruptPregame();
                    }
                    break;
                case InGame:
                    if (players.size() == 1) {
                        winGame();
                    } else {
                        String playersString = players.stream().map(HumanEntity::getName).collect(Collectors.joining(", "));
                        int killerIndex = tpFroms.indexOf(player);
                        if (killerIndex != -1) {
                            if (lastSwapTime.isPresent()) {
                                aresonDeathSwap.messages.sendPlainMessageDelayed(
                                        player,
                                        "arena-kill",
                                        5,
                                        StringPair.of("%player%", player.getName()),
                                        StringPair.of("%killer%", tpTos.get(killerIndex).getName())
                                );
                                aresonDeathSwap.getLogger().info("Player " + player.getName() + " killed by " + tpTos.get(killerIndex) + " in arena " + arenaName);
                            }
                        }
                        ArrayList<Player> copiedPlayers = new ArrayList<>(players);
                        copiedPlayers.forEach(messagePlayer -> {
                                    if (killerIndex != -1) {
                                        if (lastSwapTime.isPresent()) {
                                            if (Duration.between(lastSwapTime.get(), LocalDateTime.now()).getSeconds() < 10) {
                                                aresonDeathSwap.messages.sendPlainMessageDelayed(
                                                        messagePlayer,
                                                        "arena-kill",
                                                        5,
                                                        StringPair.of("%player%", player.getName()),
                                                        StringPair.of("%killer%", tpTos.get(killerIndex).getName())
                                                );
                                            } else {
                                                aresonDeathSwap.messages.sendPlainMessageDelayed(
                                                        messagePlayer,
                                                        "arena-kill-solo",
                                                        5,
                                                        StringPair.of("%player%", player.getName())
                                                );
                                            }
                                        } else {
                                            aresonDeathSwap.messages.sendPlainMessageDelayed(
                                                    messagePlayer,
                                                    "arena-kill-solo",
                                                    5,
                                                    StringPair.of("%player%", player.getName())
                                            );
                                        }
                                    }
                                    aresonDeathSwap.messages.sendPlainMessageDelayed(
                                            messagePlayer,
                                            "arena-players-remaining",
                                            20,
                                            StringPair.of("%number%", players.size() + ""),
                                            StringPair.of("%players%", playersString)
                                    );
                                }
                        );
                    }
                    break;
            }
        }
    }

    public void winGame() {
        arenaStatus = Ending;
        placeholders.setArenaStatus(Ending);

        if (players.size() > 0) {
            Player winnerPlayer = players.stream().findFirst().get();
            aresonDeathSwap.getServer().getOnlinePlayers().forEach(player ->
                    aresonDeathSwap.messages.sendPlainMessageDelayed(player, "victory-message", 5, StringPair.of("%player%", winnerPlayer.getName()))
            );
            aresonDeathSwap.teleportToLobbySpawn(winnerPlayer).whenComplete((input, trhowable) -> {
                aresonDeathSwap.sounds.winner(winnerPlayer);
                aresonDeathSwap.titles.sendLongTitle(winnerPlayer, "win");
                aresonDeathSwap.effects.winFirework(winnerPlayer);
                aresonDeathSwap.eventCall.callPlayerWin(winnerPlayer);
                aresonDeathSwap.eventCall.callPlayerEndGame(winnerPlayer);
            });
            players.clear();
        } else {
            aresonDeathSwap.getLogger().severe("Interrupting game with no remaining players");
        }
        interruptGame();
    }

    public void startPregame() {
        if (!countdownPregame.isRunning()) {
            countdownPregame.start();
            arenaStatus = ArenaStatus.Starting;
            placeholders.setArenaStatus(Starting);
            ArrayList<Player> copiedPlayers = new ArrayList<>(players);
            copiedPlayers.forEach(player -> aresonDeathSwap.sounds.startingGame(player));
            spawns.forEach(location -> location.getChunk().load());
        }
    }

    public ArenaStatus getArenaStatus() {
        return arenaStatus;
    }
}
