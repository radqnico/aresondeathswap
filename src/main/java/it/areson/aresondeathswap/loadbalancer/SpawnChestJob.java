package it.areson.aresondeathswap.loadbalancer;

import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.entity.Player;

public class SpawnChestJob implements Job {

    private final Player player;
    private final AresonDeathSwap aresonDeathSwap;

    public SpawnChestJob(Player player, AresonDeathSwap aresonDeathSwap) {
        this.player = player;
        this.aresonDeathSwap = aresonDeathSwap;
    }

    @Override
    public void compute() {
        if (Math.random() < 0.5) {
            aresonDeathSwap.loot.placeNewChestNear(player);
            aresonDeathSwap.messages.sendPlainMessage(player, "chest-spawned");
            aresonDeathSwap.sounds.chestAppear(player);
        }
    }

}
