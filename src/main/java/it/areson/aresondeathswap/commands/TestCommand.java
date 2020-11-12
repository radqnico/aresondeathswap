package it.areson.aresondeathswap.commands;

import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.util.Objects;

public class TestCommand implements CommandExecutor {

    private AresonDeathSwap aresonDeathSwap;

    public TestCommand(AresonDeathSwap plugin) {
        aresonDeathSwap = plugin;

        PluginCommand pluginCommand = aresonDeathSwap.getCommand("test");
        if (!Objects.isNull(pluginCommand)) {
            pluginCommand.setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length > 0) {
            //Tp
            Location location = aresonDeathSwap.getServer().getWorld("worldTest").getSpawnLocation();
            ((Player) commandSender).teleport(location);

            commandSender.sendMessage("Teleport");
        } else {
            //unload
            aresonDeathSwap.getServer().unloadWorld("worldTest", false);
            commandSender.sendMessage("Unload");
        }

        return true;
    }
}
