package it.areson.aresondeathswap;

import it.areson.aresondeathswap.enums.ArenaStatus;
import it.areson.aresondeathswap.utils.ArenaPlaceholders;
import it.areson.aresondeathswap.utils.Countdown;
import it.areson.aresondeathswap.utils.StringPair;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static it.areson.aresondeathswap.enums.ArenaStatus.*;

public class Arena {

    private final AresonDeathSwap aresonDeathSwap;
    private final String arenaName;
    private final Countdown countdownPregame;
    private ArrayList<Player> players;
    private Countdown countdownGame;
    private ArenaStatus arenaStatus;

    private ArenaPlaceholders placeholders;

    private ArrayList<Location> spawns;

    private int roundCounter;

    public Arena(AresonDeathSwap aresonDeathSwap, String arenaName) {
        this.aresonDeathSwap = aresonDeathSwap;
        this.arenaName = arenaName;
        this.players = new ArrayList<>();
        this.arenaStatus = Waiting;
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

        this.countdownGame = new Countdown(aresonDeathSwap,
                randomTeleportTime(),
                () -> {
                    rotatePlayers();
                    countdownGame.setTime(randomTeleportTime());
                    countdownGame.start();
                    roundCounter++;
                    if(roundCounter>aresonDeathSwap.MAX_ROUNDS){
                        witherPlayers();
                    } else {
                        players.forEach(player -> {
                            aresonDeathSwap.messages.sendPlainMessage(
                                    player,
                                    "rounds-remaining",
                                    StringPair.of("%remaining%", (aresonDeathSwap.MAX_ROUNDS-roundCounter)+"")
                            );
                        });
                    }
                },
                () -> {
                    // Unload

                    aresonDeathSwap.reloadArenaWorld(arenaName);
                    arenaStatus = Waiting;
                    placeholders.setArenaStatus(Waiting);
                    aresonDeathSwap.getLogger().info("Game on '" + arenaName + "' interrupted");
                },
                5,
                10,
                aresonDeathSwap.messages.getPlainMessage("countdown-swap-message"),
                this,
                aresonDeathSwap.messages.getPlainMessage("countdown-prepare-message")
        );
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
        int dx = (random.nextBoolean() ? 1 : -1) * random.nextInt(10000);
        int dz = (random.nextBoolean() ? 1 : -1) * random.nextInt(10000);
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
                aresonDeathSwap.getServer().dispatchCommand(aresonDeathSwap.getServer().getConsoleSender(), "execute as " + player.getName() + " run function deathsawpsong:stop");
                try {
                    Location removedSpawn = spawns.remove(0);
                    player.teleport(removedSpawn);
                    aresonDeathSwap.loot.placeNewChestNear(player);
                    aresonDeathSwap.messages.sendPlainMessage(player, "chest-spawned");
                    aresonDeathSwap.sounds.openChest(player.getLocation());
                } catch (IndexOutOfBoundsException e) {
                    player.teleport(getRandomLocationAroundSpawn(world));
                }
                aresonDeathSwap.sounds.gameStarted(player);
                aresonDeathSwap.titles.sendLongTitle(player, "start");
                aresonDeathSwap.eventCall.callPlayerStartGame(player);
            });
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
            players.forEach(player -> aresonDeathSwap.sounds.startingGameInterrupted(player));
        }
    }

    public void interruptGame() {
        World world = aresonDeathSwap.getServer().getWorld(arenaName);
        if (world != null) {
            for (Player player : world.getPlayers()) {
                aresonDeathSwap.teleportToLobbySpawn(player);
            }
            spawns.clear();
        } else {
            aresonDeathSwap.getLogger().severe("Error while getting the world while teleporting players");
        }

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
            Player player = players.get(i);
            player.teleport(newLocations.get(i));
            if (Math.random() < 0.5) {
                aresonDeathSwap.loot.placeNewChestNear(player);
                aresonDeathSwap.messages.sendPlainMessage(player, "chest-spawned");
                aresonDeathSwap.sounds.openChest(player.getLocation());
            }
            aresonDeathSwap.sounds.teleport(player);
            aresonDeathSwap.titles.sendShortTitle(player, "swap");
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
                        players.forEach(messagePlayer ->
                                aresonDeathSwap.messages.sendPlainMessageDelayed(
                                        messagePlayer,
                                        "arena-players-remaining",
                                        5,
                                        StringPair.of("%number%", players.size() + "")
                                )
                        );
                    }
                    break;
            }
        }
    }

    public void winGame() {
        if (players.size() > 0) {
            interruptGame();
            Player winnerPlayer = players.stream().findFirst().get();
            aresonDeathSwap.getServer().getOnlinePlayers().forEach(player ->
                    aresonDeathSwap.messages.sendPlainMessageDelayed(player, "victory-message", 5, StringPair.of("%player%", winnerPlayer.getName()))
            );
            aresonDeathSwap.sounds.winner(winnerPlayer);
            aresonDeathSwap.titles.sendLongTitle(winnerPlayer, "win");
            aresonDeathSwap.effects.winFirework(winnerPlayer);
            aresonDeathSwap.eventCall.callPlayerWin(winnerPlayer);
            aresonDeathSwap.eventCall.callPlayerEndGame(winnerPlayer);
            players.clear();

            arenaStatus = Ending;
            placeholders.setArenaStatus(Ending);


        } else {
            aresonDeathSwap.getLogger().severe("Winningg game with no remaining players");
        }
    }

    public void startPregame() {
        if (!countdownPregame.isRunning()) {
            countdownPregame.start();
            arenaStatus = ArenaStatus.Starting;
            placeholders.setArenaStatus(Starting);
            players.forEach(player -> {
                aresonDeathSwap.sounds.startingGame(player);
            });
            spawns.forEach(location -> location.getChunk().load());
        }
    }

    public ArenaStatus getArenaStatus() {
        return arenaStatus;
    }
}
