package it.areson.aresondeathswap.player;

import it.areson.aresondeathswap.AresonDeathSwap;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PlayerStatsPlaceholders extends PlaceholderExpansion {

    @Override
    public @NotNull
    String getIdentifier() {
        return "ds-player";
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

        Player player = offlinePlayer.getPlayer();
        if (player != null) {
            DeathswapPlayer deathswapPlayer = AresonDeathSwap.instance.getDeathswapPlayerManager().getDeathswapPlayer(player);
            if (identifier.equals("kills")) {
                return deathswapPlayer.getKillCount() + "";
            }

            if (identifier.equals("gamesPlayed")) {
                return deathswapPlayer.getGamesPlayed() + "";
            }

            if (identifier.equals("wins")) {
                return (deathswapPlayer.getGamesPlayed() - deathswapPlayer.getDeathCount()) + "";
            }

            if (identifier.equals("deaths")) {
                return deathswapPlayer.getDeathCount() + "";
            }
        }


        return null;
    }

}
