package it.areson.aresondeathswap.player;

import it.areson.aresondeathswap.AresonDeathSwap;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LeaderboardsPlaceholders extends PlaceholderExpansion {

    @Override
    public @NotNull
    String getIdentifier() {
        return "ds-lb";
    }

    @Override
    public @NotNull
    String getAuthor() {
        return "Areson";
    }

    @Override
    public @NotNull
    String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String identifier) {

        String[] split = identifier.split("-");
        if (split.length != 2) {
            return null;
        }
        int position;
        String stat = split[0];
        try {
            position = Integer.parseInt(split[1]);
        } catch (NumberFormatException e) {
            return null;
        }

        if (stat.equalsIgnoreCase("wins")) {
            List<DeathswapPlayer> deathswapPlayerArrayList = AresonDeathSwap.instance.getDeathswapPlayerManager().getGateway().getAll(true);
            deathswapPlayerArrayList.sort(Comparator.comparingInt(DeathswapPlayer::getWinsCount));
            Collections.reverse(deathswapPlayerArrayList);

            if (position > deathswapPlayerArrayList.size()) {
                return "Nessun dato";
            } else {
                return deathswapPlayerArrayList.get(position - 1).getNickName() + " " + deathswapPlayerArrayList.get(position - 1).getWinsCount();
            }
        }

        if (stat.equalsIgnoreCase("aggressive")) {
            List<DeathswapPlayer> deathswapPlayerArrayList = AresonDeathSwap.instance.getDeathswapPlayerManager().getGateway().getAll(true);
            deathswapPlayerArrayList.sort(Comparator.comparingInt(DeathswapPlayer::getKillCount));
            Collections.reverse(deathswapPlayerArrayList);

            if (position > deathswapPlayerArrayList.size()) {
                return "Nessun dato";
            } else {
                return deathswapPlayerArrayList.get(position - 1).getNickName() + " " + deathswapPlayerArrayList.get(position - 1).getKillCount();
            }
        }

        if (stat.equalsIgnoreCase("winrate")) {
            List<DeathswapPlayer> deathswapPlayerArrayList = AresonDeathSwap.instance.getDeathswapPlayerManager().getGateway().getAll(true);
            deathswapPlayerArrayList.sort(Comparator.comparingDouble(DeathswapPlayer::getWinRate));
            Collections.reverse(deathswapPlayerArrayList);

            if (position > deathswapPlayerArrayList.size()) {
                return "Nessun dato";
            } else {
                return deathswapPlayerArrayList.get(position - 1).getNickName() + " " + Math.round(deathswapPlayerArrayList.get(position - 1).getWinRate() * 100.0) / 100.0;
            }
        }

        if (stat.equalsIgnoreCase("kd")) {
            List<DeathswapPlayer> deathswapPlayerArrayList = AresonDeathSwap.instance.getDeathswapPlayerManager().getGateway().getAll(true);
            deathswapPlayerArrayList.sort(Comparator.comparingDouble(DeathswapPlayer::getKD));
            Collections.reverse(deathswapPlayerArrayList);

            if (position > deathswapPlayerArrayList.size()) {
                return "Nessun dato";
            } else {
                return deathswapPlayerArrayList.get(position - 1).getNickName() + " " + Math.round(deathswapPlayerArrayList.get(position - 1).getWinRate() * 100.0) / 100.0;
            }
        }

        return null;
    }

}
