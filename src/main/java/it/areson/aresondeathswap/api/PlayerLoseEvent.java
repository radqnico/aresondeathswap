package it.areson.aresondeathswap.api;

import it.areson.aresondeathswap.Arena;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerLoseEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;

    public PlayerLoseEvent(Player player) {
        this.player = player;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public Player getPlayer() {
        return player;
    }
}
