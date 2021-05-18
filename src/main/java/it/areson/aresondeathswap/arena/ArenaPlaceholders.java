package it.areson.aresondeathswap.arena;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class ArenaPlaceholders extends PlaceholderExpansion {

    private final Arena arena;

    public ArenaPlaceholders(Arena arena) {
        this.arena = arena;
    }

    @Override
    public @NotNull
    String getIdentifier() {
        return "ds-arena-" + arena.getArenaName();
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
    public String onRequest(OfflinePlayer player, String identifier) {

        if (identifier.equals("players")) {
            return arena.getPlayers().size() + "";
        }

        if (identifier.equals("minplayers")) {
            return arena.getMinPlayers() + "";
        }

        if (identifier.equals("status")) {
            switch (arena.getArenaStatus()) {
                case CLOSED:
                    return "Chiusa";
                case OPEN:
                    return "Aperta";
                case STARTING:
                    return "In avvio";
                case IN_GAME:
                    return "In gioco";
            }
        }

        if (identifier.equals("rounds")) {
            return arena.getRemainingRounds() + "";
        }

        return null;
    }
}
