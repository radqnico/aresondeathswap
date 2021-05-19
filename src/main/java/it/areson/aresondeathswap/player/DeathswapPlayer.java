package it.areson.aresondeathswap.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Optional;

public class DeathswapPlayer {

    private String nickName;
    private int killCount;
    private int deathCount;
    private long secondsPlayed;
    private int gamesPlayed;

    public DeathswapPlayer(String nickName, int killCount, int deathCount, long secondsPlayed, int gamesPlayed) {
        this.nickName = nickName;
        this.killCount = killCount;
        this.deathCount = deathCount;
        this.secondsPlayed = secondsPlayed;
        this.gamesPlayed = gamesPlayed;
    }

    public Optional<Player> getActualPlayer() {
        return Optional.ofNullable(Bukkit.getPlayer(nickName));
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getKillCount() {
        return killCount;
    }

    public void setKillCount(int killCount) {
        this.killCount = killCount;
    }

    public int getDeathCount() {
        return deathCount;
    }

    public void setDeathCount(int deathCount) {
        this.deathCount = deathCount;
    }

    public long getSecondsPlayed() {
        return secondsPlayed;
    }

    public void setSecondsPlayed(long secondsPlayed) {
        this.secondsPlayed = secondsPlayed;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getWinsCount() {
        return gamesPlayed - deathCount;
    }

    public double getWinRate() {
        if (gamesPlayed == 0) {
            return 0;
        } else {
            return (double) getWinsCount() / gamesPlayed;
        }
    }

    public double getKD() {
        if (deathCount == 0) {
            return 0;
        } else {
            return (double) killCount / deathCount;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeathswapPlayer)) return false;
        DeathswapPlayer that = (DeathswapPlayer) o;
        return Objects.equals(nickName, that.nickName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nickName);
    }
}
