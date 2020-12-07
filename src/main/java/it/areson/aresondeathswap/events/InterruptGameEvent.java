package it.areson.aresondeathswap.events;

import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class InterruptGameEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final String playerName;

    public InterruptGameEvent(String playerName) {
        this.playerName = playerName;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

}
