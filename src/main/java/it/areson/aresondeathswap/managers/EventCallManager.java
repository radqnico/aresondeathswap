package it.areson.aresondeathswap.managers;

import it.areson.aresondeathswap.AresonDeathSwap;
import it.areson.aresondeathswap.api.PlayerEndGameEvent;
import it.areson.aresondeathswap.api.PlayerLoseEvent;
import it.areson.aresondeathswap.api.PlayerStartGameEvent;
import it.areson.aresondeathswap.api.PlayerWinEvent;
import org.bukkit.entity.Player;

public class EventCallManager {

    private AresonDeathSwap aresonDeathSwap;

    public EventCallManager(AresonDeathSwap aresonDeathSwap) {
        this.aresonDeathSwap = aresonDeathSwap;
    }

    public void callPlayerWin(Player player){
        aresonDeathSwap.getServer().getPluginManager().callEvent(new PlayerWinEvent(player));
    }

    public void callPlayerLose(Player player){
        aresonDeathSwap.getServer().getPluginManager().callEvent(new PlayerLoseEvent(player));
    }

    public void callPlayerStartGame(Player player){
        aresonDeathSwap.getServer().getPluginManager().callEvent(new PlayerStartGameEvent(player));
    }

    public void callPlayerEndGame(Player player){
        aresonDeathSwap.getServer().getPluginManager().callEvent(new PlayerEndGameEvent(player));
    }
}
