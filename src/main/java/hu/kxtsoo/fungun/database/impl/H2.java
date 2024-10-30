package hu.kxtsoo.fungun.database.impl;

import hu.kxtsoo.fungun.database.DatabaseInterface;
import org.bukkit.plugin.java.JavaPlugin;
import org.h2.jdbc.JdbcConnection;

import java.io.File;
import java.sql.*;
import java.util.Properties;

public class H2 implements DatabaseInterface {

    private final JavaPlugin plugin;
    private Connection connection;

    public H2(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() throws SQLException {
        try {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            String url = "jdbc:h2:" + new File(dataFolder, "data").getAbsolutePath() + ";mode=MySQL";
            Properties props = new Properties();
            connection = new JdbcConnection(url, props, null, null, false);

            connection.setAutoCommit(true);

            createTables();
        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to the H2 database", e);
        }
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void createTables() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS fungun_users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "uuid VARCHAR(36) NOT NULL, " +
                    "selected_effect VARCHAR(255), " +
                    "selected_ability VARCHAR(255), " +
                    "UNIQUE (uuid)" +
                    ");");
        }
    }

    @Override
    public void saveSelectedEffect(String uuid, String effect) throws SQLException {
        String query = "INSERT INTO fungun_users (uuid, selected_effect) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE selected_effect = VALUES(selected_effect)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, uuid);
            ps.setString(2, effect);
            ps.executeUpdate();
        }
    }

    @Override
    public String getSelectedEffect(String uuid) throws SQLException {
        String query = "SELECT selected_effect FROM fungun_users WHERE uuid = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("selected_effect");
                }
            }
        }
        return null;
    }

    @Override
    public boolean isEffectSelected(String uuid, String effect) throws SQLException {
        String query = "SELECT 1 FROM fungun_users WHERE uuid = ? AND selected_effect = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, uuid);
            ps.setString(2, effect);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    public void saveSelectedAbility(String uuid, String ability) throws SQLException {
        String query = "MERGE INTO fungun_users (uuid, selected_ability) KEY (uuid) VALUES (?, ?)";
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
