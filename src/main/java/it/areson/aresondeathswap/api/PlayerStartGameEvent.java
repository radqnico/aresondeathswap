package it.areson.aresondeathswap.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class PlayerStartGameEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final LocalDateTime dateTime;

    public PlayerStartGameEvent(Player player) {
        this.player = player;
        this.dateTime = LocalDateTime.now();
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

    public LocalDateTime getDateTime() {
        return dateTime;
    }
}
