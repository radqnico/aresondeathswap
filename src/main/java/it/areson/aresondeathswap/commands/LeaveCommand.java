package it.areson.aresondeathswap.commands;

import it.areson.aresondeathswap.AresonDeathSwap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;

import java.util.Objects;

@SuppressWarnings("NullableProblems")
public class LeaveCommand implements CommandExecutor {

    private final AresonDeathSwap aresonDeathSwap;

    public LeaveCommand(AresonDeathSwap plugin) {
        aresonDeathSwap = plugin;

        PluginCommand pluginCommand = aresonDeathSwap.getCommand("leave");
        if (!Objects.isNull(pluginCommand)) {
            pluginCommand.setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] arguments) {
        if (commandSender instanceof Player) {
            Player player = (Player) commandSender;
            if (aresonDeathSwap.playerIsInAnArena(player)) {
                aresonDeathSwap.removePlayerFromArenas(player);
                aresonDeathSwap.messages.sendPlainMessage(player, "arena-leaved");
            } else {
                aresonDeathSwap.messages.sendPlainMessage(player, "missing-arena-name");
                //TODO Nico: suono?
            }
        } else {
            commandSender.sendMessage("Comando eseguibile solo da player");
        }

        return true;
    }
}
