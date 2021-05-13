package it.areson.aresondeathswap.player;

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
}
