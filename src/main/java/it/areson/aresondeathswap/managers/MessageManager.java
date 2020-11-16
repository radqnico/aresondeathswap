package it.areson.aresondeathswap.managers;

import it.areson.aresondeathswap.AresonDeathSwap;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Objects;

public class MessageManager extends FileManager {

    private final String prefix;

    public MessageManager(AresonDeathSwap plugin, String fileName) {
        super(plugin, fileName);
        prefix = getFileConfiguration().getString("prefix");
    }

    public void sendPlainMessage(Player player, String messageKey) {
        String message = getFileConfiguration().getString(messageKey);
        if (Objects.nonNull(message)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cError: '" + messageKey + "' message does not exists!"));
        }
    }

    public void sendPlainMessageDelayed(Player player, String messageKey, long delayTicks) {
        aresonDeathSwap.getServer().getScheduler().scheduleSyncDelayedTask(
                aresonDeathSwap,
                () -> {
                    String message = getFileConfiguration().getString(messageKey);
                    if (Objects.nonNull(message)) {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
                    } else {
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cError: '" + messageKey + "' message does not exists!"));
                    }
                },
                delayTicks
        );

    }

    public void sendJsonMessage(Player player, String messageKey) {
        String message = getFileConfiguration().getString(messageKey);
        if (Objects.nonNull(message)) {
            BaseComponent[] rawMessage = ComponentSerializer.parse(message);
            player.spigot().sendMessage(rawMessage);
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + "&cError: '" + messageKey + "' message does not exists!"));
        }
    }

    public String getPlainMessage(String messageKey) {
        String message = getFileConfiguration().getString(messageKey);
        if (Objects.nonNull(message)) {
            return ChatColor.translateAlternateColorCodes('&', prefix + message);
        } else {
            return ChatColor.translateAlternateColorCodes('&', prefix + "&cError: '" + messageKey + "' message does not exists!");
        }
    }

    public String getPlainMessage(String messageKey, boolean prefix) {
        String message = getFileConfiguration().getString(messageKey);
        if (Objects.nonNull(message)) {
            if (prefix) {
                return ChatColor.translateAlternateColorCodes('&', prefix + message);
            } else {
                return ChatColor.translateAlternateColorCodes('&', message);
            }
        } else {
            return ChatColor.translateAlternateColorCodes('&', prefix + "&cError: '" + messageKey + "' message does not exists!");
        }
    }

    public BaseComponent[] getJsonMessage(String messageKey) {
        String message = getFileConfiguration().getString(messageKey);
        if (Objects.nonNull(message)) {
            return ComponentSerializer.parse(message);
        } else {
            return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', prefix + "&cError: '" + messageKey + "' message does not exists!"));
        }
    }
}
