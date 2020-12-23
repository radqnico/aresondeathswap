package it.areson.aresondeathswap.managers;

import it.areson.aresondeathswap.utils.StringPair;
import org.bukkit.entity.Player;

import java.util.TreeMap;

public class TitlesManager {

    private final MessageManager messageManager;
    private final TreeMap<String, StringPair> titles;

    public TitlesManager(MessageManager messageManager) {
        this.messageManager = messageManager;
        titles = new TreeMap<>();
        readAllTitles();
    }

    private void readAllTitles() {
        this.messageManager.getFileConfiguration().getKeys(false).forEach(key -> {
            if (key.startsWith("title-")) {
                String titleName = key.replace("title-", "");
                titles.put(titleName, getTitleAndSubtitle(titleName));
            }
        });
    }

    private StringPair getTitleAndSubtitle(String titleName) {
        String title = messageManager.getPlainMessage("title-" + titleName, false);
        String subTitle = messageManager.getPlainMessage("subtitle-" + titleName, false);
        return StringPair.of(title, subTitle);
    }

    public void sendShortTitle(Player player, String titleName) {
        StringPair title = titles.get(titleName);
        player.sendTitle(title.getLeft(), title.getRight(), 10, 20, 10);
    }

    public void sendLongTitle(Player player, String titleName) {
        StringPair title = titles.get(titleName);
        player.sendTitle(title.getLeft(), title.getRight(), 20, 60, 20);
    }
}
