package it.areson.aresondeathswap.managers;

import it.areson.aresondeathswap.utils.Pair;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class TitlesManager {

    private MessageManager messageManager;
    private HashMap<String, Pair<String, String>> titles;

    public TitlesManager(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    private void readAllTitles() {
        this.messageManager.getFileConfiguration().getKeys(false).forEach(key -> {
            if (key.startsWith("title-")) {
                String titleName = key.replace("title-", "");
                titles.put(titleName, getTitleAndSubtitle(titleName));
            }
        });
    }

    private Pair<String, String> getTitleAndSubtitle(String titleName) {
        String title = messageManager.getPlainMessage("title-" + titleName, false);
        String subTitle = messageManager.getPlainMessage("subtitle-" + titleName, false);
        return Pair.of(title, subTitle);
    }

    public void sendShortTitle(Player player, String titleName) {
        Pair<String, String> title = titles.get(titleName);
        player.sendTitle(title.getLeft(), title.getRight(), 10, 20, 10);
    }

    public void sendLongTitle(Player player, String titleName) {
        Pair<String, String> title = titles.get(titleName);
        player.sendTitle(title.getLeft(), title.getRight(), 20, 60, 20);
    }
}
