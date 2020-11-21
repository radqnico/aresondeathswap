package it.areson.aresondeathswap.utils;

import it.areson.aresondeathswap.enums.ArenaStatus;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ArenaPlaceholders extends PlaceholderExpansion {

    private ArenaStatus arenaStatus;
    private final String arenaName;
    private final ArrayList<Player> players;
    private String roundsRemainingString;

    public ArenaPlaceholders(ArenaStatus arenaStatus, String arenaName, ArrayList<Player> players) {
        this.arenaStatus = arenaStatus;
        this.arenaName = arenaName;
        this.players = players;
        this.roundsRemainingString = "Non in gioco";
    }

    public void setArenaStatus(ArenaStatus arenaStatus){
        this.arenaStatus = arenaStatus;
    }

    public void setRoundsRemainingString(String roundsRemaining) {
        this.roundsRemainingString = roundsRemaining;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "ds-" + arenaName;
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
            switch (arenaStatus){
                case Waiting:
                    return "Aperta";
                case Starting:
                    return "In avvio";
                case InGame:
                    return "In gioco";
                case Ending:
                    return "Riavvio";
            }
        }

        if(identifier.equals("rounds")){
            return roundsRemainingString;
        }

        return null;
    }
}
