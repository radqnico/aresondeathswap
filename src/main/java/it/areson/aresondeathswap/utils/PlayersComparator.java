package it.areson.aresondeathswap.utils;

import org.bukkit.entity.Player;

import java.util.Comparator;

public class PlayersComparator implements Comparator<Player> {

    @Override
    public int compare(Player player1, Player player2) {
        return player1.getUniqueId().compareTo(player2.getUniqueId());
    }

}
