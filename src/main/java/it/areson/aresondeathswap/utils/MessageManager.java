package it.areson.aresondeathswap.utils;

import it.areson.aresondeathswap.AresonDeathSwap;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class MessageManager extends FileManager {

    private final String prefix;
    private final String messageNotFound;

    public MessageManager(AresonDeathSwap plugin, String fileName) {
        super(plugin, fileName);
        prefix = getFileConfiguration().getString("prefix", "");
        messageNotFound = prefix + ChatColor.RED + "Errore: messaggio non trovato";
    }

    public void sendPlainMessage(CommandSender commandSender, String messageKey) {
        String message = getFileConfiguration().getString(messageKey);
        if (Objects.nonNull(message)) {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
        } else {
            commandSender.sendMessage(messageNotFound);
        }
    }

    public void sendJsonMessage(Player player, String messageKey) {
        String message = getFileConfiguration().getString(messageKey);
        if (Objects.nonNull(message)) {
            BaseComponent[] rawMessage = ComponentSerializer.parse(message);
            player.spigot().sendMessage(rawMessage);
        } else {
            player.sendMessage(messageNotFound);
        }
    }

    @SafeVarargs
    public final String getPlainMessage(String messageKey, Pair<String, String>... substitutions) {
        String message = getFileConfiguration().getString(messageKey);
        if (Objects.nonNull(message)) {
            for (Pair<String, String> stringPair : substitutions) {
                message = message.replaceAll(stringPair.left(), stringPair.right());
            }

            return ChatColor.translateAlternateColorCodes('&', prefix + message);
        } else {
            return messageNotFound;
        }
    }

    @SafeVarargs
    public final void sendPlainMessage(CommandSender commandSender, String messageKey, Pair<String, String>... substitutions) {
        String message = getFileConfiguration().getString(messageKey);
        if (Objects.nonNull(message)) {
            for (Pair<String, String> stringPair : substitutions) {
                message = message.replaceAll(stringPair.left(), stringPair.right());
            }

            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
        } else {
            commandSender.sendMessage(messageNotFound);
        }
    }

    public String getPlainMessageNoPrefix(String messageKey) {
        String message = getFileConfiguration().getString(messageKey);
        if (Objects.nonNull(message)) {
            return ChatColor.translateAlternateColorCodes('&', message);
        } else {
            return ChatColor.translateAlternateColorCodes('&', "&cError: '" + messageKey + "' message does not exists!");
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

    public BaseComponent[] getJsonMessage(String messageKey) {
        String message = getFileConfiguration().getString(messageKey);
        if (Objects.nonNull(message)) {
            return ComponentSerializer.parse(message);
        } else {
            return TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', prefix + "&cError: '" + messageKey + "' message does not exists!"));
        }
    }
}
