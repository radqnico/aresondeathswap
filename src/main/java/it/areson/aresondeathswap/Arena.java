package it.areson.aresondeathswap;

import it.areson.aresondeathswap.enums.ArenaStatus;
import it.areson.aresondeathswap.loadbalancer.LoadBalancer;
import it.areson.aresondeathswap.loadbalancer.SpawnChestJob;
import it.areson.aresondeathswap.loadbalancer.TeleportJob;
import it.areson.aresondeathswap.utils.*;
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

public class Arena {

    private final AresonDeathSwap aresonDeathSwap;
    private final String arenaName;
    private final CDTaskSeries countdownPregame;
    private final ArrayList<Player> players;
    private final ArenaPlaceholders placeholders;
    private final ArrayList<Location> spawns;
    private final CDTaskSeries countdownGame;
    private ArenaStatus arenaStatus;
    private LocalDateTime lastSwapTime;
    private final Map<String, String> lastSwaps;

    private int roundCounter;

    public Arena(AresonDeathSwap aresonDeathSwap, String arenaName) {
        this.aresonDeathSwap = aresonDeathSwap;
        this.arenaName = arenaName;
        this.players = new ArrayList<>();
        this.arenaStatus = Waiting;
        lastSwapTime = LocalDateTime.MIN;
        roundCounter = 0;
        spawns = new ArrayList<>();
        placeholders = new ArenaPlaceholders(this.arenaStatus, this.arenaName, this.players);
        placeholders.register();
        lastSwaps = new HashMap<>();
        //Countdowns
        this.countdownPregame = new CDTaskSeries(
                aresonDeathSwap,
                aresonDeathSwap.STARTING_TIME,
                () -> {
                    try {
                        arenaStatus = InGame;
                        placeholders.setArenaStatus(InGame);
                        startGame();
                    } catch (Exception e) {
                        System.out.println("Countdown end run error :");
                        e.printStackTrace(System.out);
                    }
                },
                () -> {
                    try {
                        arenaStatus = Waiting;
                        placeholders.setArenaStatus(Waiting);
                        ArrayList<Player> copiedPlayers = new ArrayList<>(players);
                        copiedPlayers.forEach(player -> aresonDeathSwap.messages.sendPlainMessage(player, "countdown-interrupted"));
                    } catch (Exception e) {
                        System.out.println("Countdown interrupt run error :");
                        e.printStackTrace(System.out);
                    }
                },
                15,
                aresonDeathSwap.messages.getPlainMessage("countdown-starting-message"),
                this,
                aresonDeathSwap.messages.getPlainMessage("countdown-start-message")
        );

        this.countdownGame = new CDTaskSeries(
                aresonDeathSwap,
                randomTeleportTime(),
                () -> {
                    try {
                        rotatePlayers();
                        roundCounter++;
                        lastSwapTime = LocalDateTime.now();
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
                        System.out.println("Repeating task run error");
                        e.printStackTrace(System.out);
                    }
                },
                () -> aresonDeathSwap.getLogger().info("Interrupted countdownGame in arena " + arenaName),
                10,
                aresonDeathSwap.messages.getPlainMessage("countdown-swap-message"),
                this,
                null
        );
    }

    public void restartCountdownGame(){
        countdownGame.interrupt();
        int swapTime = randomTeleportTime();
        countdownGame.setCountdownTime(swapTime);
        countdownGame.start();
        aresonDeathSwap.getLogger().info("Started new countdownGame in arena " + arenaName + " with " + swapTime + " seconds");
    }

    public void forceSwap() {
        try {
            lastSwapTime = LocalDateTime.now();
            rotatePlayers();
        } catch (Exception e) {
            System.out.println("Force swap error");
            e.printStackTrace(System.out);
        }
    }

    public void rotatePlayers() {

        LoadBalancer swapsLoadBalancer = new LoadBalancer("TELEPORTS " + arenaName);
        aresonDeathSwap.getLogger().info("Rotating " + players.size() + " players in arena " + arenaName);

        ArrayList<Player> copiedPlayers = new ArrayList<>(players);
        Map<Player, Location> playerDestination = new HashMap<>();

        Collections.shuffle(copiedPlayers);

        int index = 0;
        int size;
        do {
            playerDestination.clear();
            lastSwaps.clear();
            size = copiedPlayers.size();
            for (Player player : copiedPlayers) {
                if (index == size - 1) {
                    playerDestination.put(player, copiedPlayers.get(0).getLocation().clone());
                    lastSwaps.put(player.getName(), copiedPlayers.get(0).getName());
                } else {
                    playerDestination.put(player, copiedPlayers.get(index + 1).getLocation().clone());
                    lastSwaps.put(player.getName(), copiedPlayers.get(index + 1).getName());
                }
                index++;
            }
        } while (size != copiedPlayers.size());

        playerDestination.forEach(((player, location) -> swapsLoadBalancer.addJob(
                new TeleportJob(player, location, (input, exception) -> {
                    if (input) {
                        if (!player.getWorld().getName().equals(aresonDeathSwap.MAIN_WORLD_NAME)) {
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
                })
        )));

        swapsLoadBalancer.start(aresonDeathSwap).whenComplete(
                (totalTicks, exception) -> aresonDeathSwap.getLogger().info("Rotating " + players.size() + " players in arena " + arenaName + " took " + totalTicks + " ticks")
        );

        restartCountdownGame();
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
            LoadBalancer loadBalancer = new LoadBalancer("Start game teleports");
            placeholders.setRoundsRemainingString(roundCounter + "/" + aresonDeathSwap.MAX_ROUNDS);
            copiedPlayers.forEach(player -> {
                try {
                    Location removedSpawn = spawns.remove(0);
                    aresonDeathSwap.effects.joinedArena(player);
                    loadBalancer.addJob(new TeleportJob(player, removedSpawn, (result, exception) -> teleportInArenaEffects(result, player)));
                    player.getInventory().clear();
                } catch (IndexOutOfBoundsException e) {
                    loadBalancer.addJob(new TeleportJob(player, getRandomLocationAroundSpawn(world), (result, exception) -> teleportInArenaEffects(result, player)));
                }
            });
            loadBalancer.start(aresonDeathSwap).whenComplete((totalTicks, exception) -> aresonDeathSwap.getLogger().severe("Inserted players in arena '" + arenaName + "' in " + totalTicks + " ticks"));
            countdownGame.start();
            this.arenaStatus = ArenaStatus.InGame;
        } else {
            aresonDeathSwap.getLogger().severe("Cannot found arena world");
        }
    }

    private void teleportInArenaEffects(Boolean result, Player player) {
        if (result) {
            aresonDeathSwap.loot.placeNewChestNear(player);
            aresonDeathSwap.messages.sendPlainMessage(player, "chest-spawned");
            aresonDeathSwap.sounds.openChest(player.getLocation());
            aresonDeathSwap.sounds.gameStarted(player);
            aresonDeathSwap.titles.sendLongTitle(player, "start");
            aresonDeathSwap.eventCall.callPlayerStartGame(player);
        } else {
            aresonDeathSwap.messages.sendPlainMessage(player, "teleport-fail");
            aresonDeathSwap.removePlayerFromArenas(player);
        }
    }

    private int randomTeleportTime() {
        Random random = new Random();
        return random.nextInt(aresonDeathSwap.MAX_SWAP_TIME_SECONDS - aresonDeathSwap.MIN_SWAP_TIME_SECONDS) + aresonDeathSwap.MIN_SWAP_TIME_SECONDS;
    }

    public void interruptPregame() {
        countdownPregame.interrupt();
        ArrayList<Player> copiedPlayers = new ArrayList<>(players);
        copiedPlayers.forEach(player -> aresonDeathSwap.sounds.startingGameInterrupted(player));
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
                countdownGame.interrupt();
                aresonDeathSwap.loot.removeChestOfWorld(arenaName);

                aresonDeathSwap.getServer().getScheduler().scheduleSyncDelayedTask(aresonDeathSwap, () -> {
                    if (world != null) {
                        aresonDeathSwap.getServer().getLogger().warning("Players on " + arenaName + ": " + world.getPlayers());
                        while (world.getPlayers().size()>0){
                            aresonDeathSwap.teleportToLobbySpawn(world.getPlayers().get(0));
                        }
                    }
                    if (aresonDeathSwap.getServer().unloadWorld(arenaName, false)) {
                        aresonDeathSwap.getServer().getScheduler().scheduleSyncDelayedTask(aresonDeathSwap, () -> {
                            if (aresonDeathSwap.loadArenaWorld(arenaName)) {
                                arenaStatus = Waiting;
                                placeholders.setArenaStatus(Waiting);
                                placeholders.setRoundsRemainingString("Non in gioco");
                                lastSwapTime = LocalDateTime.MIN;
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
            ArrayList<Player> copiedPlayers = new ArrayList<>(players);
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
                        copiedPlayers.forEach(messagePlayer -> {
                                    if (Duration.between(lastSwapTime, LocalDateTime.now()).getSeconds() < 15) {
                                        // Is a kill
                                        String deadPlayerName = player.getName();
                                        aresonDeathSwap.messages.sendPlainMessageDelayed(
                                                messagePlayer,
                                                "arena-kill",
                                                5,
                                                StringPair.of("%player%", deadPlayerName),
                                                StringPair.of("%killer%", lastSwaps.get(deadPlayerName))
                                        );
                                    } else {
                                        // Is not a kill
                                        aresonDeathSwap.messages.sendPlainMessageDelayed(
                                                messagePlayer,
                                                "arena-kill-solo",
                                                5,
                                                StringPair.of("%player%", player.getName())
                                        );
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
        countdownPregame.start();
        arenaStatus = ArenaStatus.Starting;
        placeholders.setArenaStatus(Starting);
        ArrayList<Player> copiedPlayers = new ArrayList<>(players);
        copiedPlayers.forEach(player -> aresonDeathSwap.sounds.startingGame(player));
        spawns.forEach(location -> location.getChunk().load());
    }

    public ArenaStatus getArenaStatus() {
        return arenaStatus;
    }

    public String getName() {
        return arenaName;
    }

}
