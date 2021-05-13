package it.areson.aresondeathswap.player;

import it.areson.aresoncore.database.DBGateway;
import it.areson.aresoncore.database.MySqlConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DeathswapPlayerGateway extends DBGateway<String, DeathswapPlayer> {

    private static DeathswapPlayerGateway instance;

    public DeathswapPlayerGateway(MySqlConnection mySqlConnection, String tableName) {
        super(mySqlConnection, tableName);
    }

    @Override
    public List<DeathswapPlayer> getAll(boolean invalidateCache) {
        String query = "SELECT * FROM " + tableName;
        List<DeathswapPlayer> deathswapPlayers = new ArrayList<>();
        try {
            Connection connection = mySqlConnection.connect();
            ResultSet resultSet = mySqlConnection.select(connection, query);
            if (invalidateCache) {
                cache.clear();
            }
            while (resultSet.next()) {
                DeathswapPlayer deathswapPlayer = resultToObject(resultSet);
                deathswapPlayers.add(deathswapPlayer);
                cache.putIfAbsent(deathswapPlayer.getNickName(), deathswapPlayer);
            }
            connection.close();
        } catch (SQLException exception) {
            mySqlConnection.printSqlExceptionDetails(exception);
        }
        return deathswapPlayers;
    }

    @Override
    public Optional<DeathswapPlayer> getById(String nickname) {
        if (Objects.equals(nickname, "")) {
            return Optional.empty();
        }

        DeathswapPlayer cached = cache.get(nickname);
        if (Objects.nonNull(cached)) {
            return Optional.of(cached);
        }

        String query = "SELECT * FROM " + tableName + " WHERE nickname='" + nickname + "'";
        Optional<DeathswapPlayer> optionalDeathswapPlayer = Optional.empty();
        try {
            Connection connection = mySqlConnection.connect();
            ResultSet resultSet = mySqlConnection.select(connection, query);
            while (resultSet.next()) {
                DeathswapPlayer deathswapPlayer = resultToObject(resultSet);
                optionalDeathswapPlayer = Optional.of(deathswapPlayer);
                cache.putIfAbsent(deathswapPlayer.getNickName(), deathswapPlayer);
            }
            connection.close();
        } catch (SQLException exception) {
            mySqlConnection.printSqlExceptionDetails(exception);
        }
        return optionalDeathswapPlayer;
    }

    @Override
    public DeathswapPlayer resultToObject(ResultSet resultSet) throws SQLException {
        String nickName = resultSet.getString("nickname");
        int killCount = resultSet.getInt("killCount");
        int deathCount = resultSet.getInt("deathCount");
        long secondsPlayed = resultSet.getLong("secondsPlayed");
        int gamesPlayed = resultSet.getInt("gamesPlayed");
        return new DeathswapPlayer(nickName, killCount, deathCount, secondsPlayed, gamesPlayed);
    }

    @Override
    public String objectToQuery(DeathswapPlayer deathswapPlayer) {
        String query;
        String formatted;
        if (Objects.equals(deathswapPlayer.getNickName(), "")) {
            query = "INSERT INTO " + tableName +
                    "(nickname, killCount, deathCount, secondsPlayed, gamesPlayed) " +
                    "VALUES ('%s', '%d', '%d', '%d', '%d')";
        } else {
            query = "UPDATE " + tableName + " " +
                    "SET nickname='%s', killCount='%s', deathCount='%s', secondsPlayed='%s', gamesPlayed='%s' " +
                    "WHERE nickname='%s'";
        }
        formatted = String.format(query,
                deathswapPlayer.getNickName(),
                deathswapPlayer.getKillCount(),
                deathswapPlayer.getDeathCount(),
                deathswapPlayer.getSecondsPlayed(),
                deathswapPlayer.getGamesPlayed(),
                deathswapPlayer.getNickName()
        );
        return formatted;
    }

    @Override
    public boolean delete(String nickname) {
        String query = "DELETE FROM " + tableName + " WHERE nickname = " + nickname;
        try {
            Connection connection = mySqlConnection.connect();
            int affectedRows = mySqlConnection.update(connection, query);
            connection.close();
            if (affectedRows > 0) {
                cache.remove(nickname);
                return true;
            }
        } catch (SQLException exception) {
            mySqlConnection.printSqlExceptionDetails(exception);
        }
        return false;
    }

    @Override
    public boolean upsert(DeathswapPlayer object) {
        String query = objectToQuery(object);
        try {
            Connection connection = mySqlConnection.connect();
            int affectedRows = mySqlConnection.update(connection, query);
            connection.close();
            if (affectedRows > 0) {
                return true;
            }
        } catch (SQLException exception) {
            mySqlConnection.printSqlExceptionDetails(exception);
        }
        return false;
    }
}
