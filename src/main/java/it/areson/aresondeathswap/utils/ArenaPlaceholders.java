package it.areson.aresondeathswap.utils;

import it.areson.aresondeathswap.enums.ArenaStatus;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ArenaPlaceholders extends PlaceholderExpansion {

    private final ArenaStatus arenaStatus;
    private final String arenaName;
    private final ArrayList<Player> players;

    public ArenaPlaceholders(ArenaStatus arenaStatus, String arenaName, ArrayList<Player> players) {
        this.arenaStatus = arenaStatus;
        this.arenaName = arenaName;
        this.players = players;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "deathswap" + arenaName.toLowerCase();
    }

    @Override
    public @NotNull String getAuthor() {
        return "Areson";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier){

        if(identifier.equals("players")){
            return players.size()+"";
        }

        if(identifier.equals("status")){
            return arenaStatus.name();
        }

        return null;
    }
}
