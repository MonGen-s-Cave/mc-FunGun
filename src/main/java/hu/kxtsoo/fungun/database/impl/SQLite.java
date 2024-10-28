package hu.kxtsoo.fungun.database.impl;

import hu.kxtsoo.fungun.database.DatabaseInterface;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;

public class SQLite implements DatabaseInterface {

    private final JavaPlugin plugin;
    private Connection connection;

    public SQLite(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() throws SQLException {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        String url = "jdbc:sqlite:" + new File(dataFolder, "database.db").getAbsolutePath();
        connection = DriverManager.getConnection(url);

        createTables();
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS fungun_users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "uuid TEXT NOT NULL, " +
                    "selected_effect TEXT, " +
                    "selected_ability TEXT, " +
                    "UNIQUE (uuid)" +
                    ");");
        }
    }

    @Override
    public void saveSelectedEffect(String uuid, String effect) throws SQLException {
        String query = "INSERT INTO fungun_users (uuid, selected_effect) VALUES (?, ?) " +
                "ON CONFLICT(uuid) DO UPDATE SET selected_effect = excluded.selected_effect";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, uuid);
            ps.setString(2, effect);
            ps.executeUpdate();
        }
    }

    @Override
    public String getSelectedEffect(String uuid) throws SQLException {
        String query = "SELECT selected_effect FROM fungun_users WHERE uuid = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("selected_effect");
            }
        }
        return null;
    }

    @Override
    public boolean isEffectSelected(String uuid, String effect) throws SQLException {
        String query = "SELECT 1 FROM fungun_users WHERE uuid = ? AND selected_effect = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, uuid);
            statement.setString(2, effect);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        }
    }

    public void saveSelectedAbility(String uuid, String ability) throws SQLException {
        String query = "INSERT INTO fungun_users (uuid, selected_ability) VALUES (?, ?) " +
                "ON CONFLICT(uuid) DO UPDATE SET selected_ability = excluded.selected_ability";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, uuid);
            ps.setString(2, ability);
            ps.executeUpdate();
        }
    }

    public String getSelectedAbility(String uuid) throws SQLException {
        String query = "SELECT selected_ability FROM fungun_users WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, uuid);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("selected_ability");
            }
        }
        return null;
    }

    public boolean isAbilitySelected(String uuid, String ability) throws SQLException {
        String query = "SELECT 1 FROM fungun_users WHERE uuid = ? AND selected_ability = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, uuid);
            ps.setString(2, ability);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}

