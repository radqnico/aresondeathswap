package it.areson.aresondeathswap;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class MessageManager extends FileManager {

    public MessageManager(AresonDeathSwap plugin, String fileName) {
        super(plugin, fileName);
    }

    public void sendPlainMessage(Player player, String messageKey) {
        String message = getFileConfiguration().getString(messageKey);
        if (Objects.nonNull(message)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lError: '" + messageKey + "' message does not exists!"));
        }
    }

    public void sendJsonMessage(Player player, String messageKey) {
        String message = getFileConfiguration().getString(messageKey);
        if (Objects.nonNull(message)) {
            BaseComponent[] rawMessage = ComponentSerializer.parse(message);
            player.spigot().sendMessage(rawMessage);
        } else {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c&lError: '" + messageKey + "' message does not exists!"));
        }
    }

    public String getPlainMessage(Player player, String messageKey) {
        String message = getFileConfiguration().getString(messageKey);
        if (Objects.nonNull(message)) {
            return ChatColor.translateAlternateColorCodes('&', message);
        } else {
            return ChatColor.translateAlternateColorCodes('&', "&c&lError: '" + messageKey + "' message does not exists!");
        }
    }

    public BaseComponent[] getJsonMessage(Player player, String messageKey) {
        String message = getFileConfiguration().getString(messageKey);
        if (Objects.nonNull(message)) {
            return ComponentSerializer.parse(message);
        } else {
            return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', "&c&lError: '" + messageKey + "' message does not exists!"));
        }
    }
}
