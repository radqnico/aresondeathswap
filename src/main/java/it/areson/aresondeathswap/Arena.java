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

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static it.areson.aresondeathswap.enums.ArenaStatus.*;

public class Arena {

    private final AresonDeathSwap aresonDeathSwap;
    private final String arenaName;
    private final Countdown countdownPregame;
    private ArrayList<Player> players;
    private DelayedRepeatingTask countdownGame;
    private ArenaStatus arenaStatus;

    private ArenaPlaceholders placeholders;

    private ArrayList<Location> spawns;

    private ArrayList<Player> tpFroms;
    private ArrayList<Player> tpTos;

    private int roundCounter;

    public Arena(AresonDeathSwap aresonDeathSwap, String arenaName) {
        this.aresonDeathSwap = aresonDeathSwap;
        this.arenaName = arenaName;
        this.players = new ArrayList<>();
        this.arenaStatus = Waiting;
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
                () -> players.forEach(player -> {
                    aresonDeathSwap.messages.sendPlainMessage(player, "countdown-interrupted");
                    arenaStatus = Waiting;
                    placeholders.setArenaStatus(Waiting);
                }),
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
                    rotatePlayers();
                    int swapTime = randomTeleportTime();
                    countdownGame.setEverySeconds(swapTime);
                    aresonDeathSwap.getLogger().info("Started new countdownGame in arena " + arenaName + " with " + swapTime + " seconds");
                    roundCounter++;
                    if (roundCounter > aresonDeathSwap.MAX_ROUNDS) {
                        witherPlayers();
                    } else {
                        players.forEach(player -> aresonDeathSwap.messages.sendPlainMessage(
                                player,
                                "rounds-remaining",
                                StringPair.of("%remaining%", (aresonDeathSwap.MAX_ROUNDS - roundCounter) + "")
                        ));
                    }
                },
                Optional.of(aresonDeathSwap.messages.getPlainMessage("countdown-swap-message")),
                players
        );
    }

    public void rotatePlayers() {
        aresonDeathSwap.getLogger().info("Rotating " + players.size() + " players in arena " + arenaName);
        tpFroms.clear();
        tpTos.clear();
        Collections.shuffle(players);
        List<Location> newLocations = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            if (i == (players.size() - 1)) {
                newLocations.add(players.get(0).getLocation().clone());
                tpTos.add(players.get(0));
            } else {
                newLocations.add(players.get(i + 1).getLocation().clone());
                tpTos.add(players.get(i + 1));
            }
        }

        for (int i = 0; i < newLocations.size(); i++) {
            Player player = players.get(i);
            tpFroms.add(player);
            player.teleportAsync(newLocations.get(i)).whenComplete((input, exception) -> {
                if (Math.random() < 0.5) {
                    aresonDeathSwap.loot.placeNewChestNear(player);
                    aresonDeathSwap.messages.sendPlainMessage(player, "chest-spawned");
                    aresonDeathSwap.sounds.openChest(player.getLocation());
                }
                aresonDeathSwap.sounds.teleport(player);
                aresonDeathSwap.titles.sendShortTitle(player, "swap");
            });
        }
        //TODO Remove log
        aresonDeathSwap.getLogger().info("Coppie di teletrasporti:");
        for(int i=0; i<tpTos.size(); i++){
            aresonDeathSwap.getLogger().info("'"+tpFroms.get(i).getName()+"' -> '"+tpTos.get(i).getName()+"'");
        }
    }

    private void witherPlayers() {
        players.forEach(player -> player.addPotionEffect(new PotionEffect(
                PotionEffectType.WITHER,
                Integer.MAX_VALUE,
                2,
                false,
                false,
                false
        )));
    }

    private Location getRandomLocationAroundSpawn(World world) {
        Location spawnLocation = world.getSpawnLocation();
        Random random = new Random();
        int dx = (random.nextBoolean() ? 1 : -1) * random.nextInt(4000);
        int dz = (random.nextBoolean() ? 1 : -1) * random.nextInt(4000);
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
            players.forEach(player -> {
                try {
                    Location removedSpawn = spawns.remove(0);
                    aresonDeathSwap.effects.joinedArena(player);
                    player.teleportAsync(removedSpawn).whenComplete((input, exception) -> {
                        aresonDeathSwap.loot.placeNewChestNear(player);
                        aresonDeathSwap.messages.sendPlainMessage(player, "chest-spawned");
                        aresonDeathSwap.sounds.openChest(player.getLocation());
                        aresonDeathSwap.sounds.gameStarted(player);
                        aresonDeathSwap.titles.sendLongTitle(player, "start");
                        aresonDeathSwap.eventCall.callPlayerStartGame(player);
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
            players.forEach(player -> aresonDeathSwap.sounds.startingGameInterrupted(player));
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
            countdownGame.stopRepeating();
            aresonDeathSwap.loot.removeChestOfWorld(arenaName);
            aresonDeathSwap.reloadArenaWorld(arenaName);
            arenaStatus = Waiting;
            placeholders.setArenaStatus(Waiting);
            aresonDeathSwap.getLogger().info("Game on '" + arenaName + "' interrupted");
        });
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
                    aresonDeathSwap.teleportToLobbySpawn(player);
                    if (players.size() == 1) {
                        winGame();
                    } else {
                        String playersString = players.stream().map(HumanEntity::getName).collect(Collectors.joining(","));
                        players.forEach(messagePlayer ->
                                aresonDeathSwap.messages.sendPlainMessageDelayed(
                                        messagePlayer,
                                        "arena-players-remaining",
                                        5,
                                        StringPair.of("%number%", players.size() + ""),
                                        StringPair.of("%players%", playersString)
                                )
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
            players.forEach(player -> aresonDeathSwap.sounds.startingGame(player));
            spawns.forEach(location -> location.getChunk().load());
        }
    }

    public ArenaStatus getArenaStatus() {
        return arenaStatus;
    }
}
