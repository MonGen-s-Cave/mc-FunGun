package hu.kxtsoo.fungun.database.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hu.kxtsoo.fungun.database.DatabaseInterface;
import hu.kxtsoo.fungun.util.ConfigUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;

public class MySQL implements DatabaseInterface {

    private final ConfigUtil configUtil;
    private final JavaPlugin plugin;
    private HikariDataSource dataSource;

    public MySQL(ConfigUtil configUtil, JavaPlugin plugin) {
        this.configUtil = configUtil;
        this.plugin = plugin;
    }

    @Override
    public void initialize() throws SQLException {
        HikariConfig hikariConfig = new HikariConfig();

        String host = configUtil.getConfig().getString("storage.host", "localhost");
        String port = configUtil.getConfig().getString("storage.port", "3306");
        String database = configUtil.getConfig().getString("storage.name", "database_name");
        String username = configUtil.getConfig().getString("storage.username", "root");
        String password = configUtil.getConfig().getString("storage.password", "");

        String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        hikariConfig.setMaximumPoolSize(configUtil.getConfig().getInt("storage.pool.maximumPoolSize", 10));
        hikariConfig.setMinimumIdle(configUtil.getConfig().getInt("storage.pool.minimumIdle", 5));
        hikariConfig.setConnectionTimeout(configUtil.getConfig().getInt("storage.pool.connectionTimeout", 30000));
        hikariConfig.setMaxLifetime(configUtil.getConfig().getInt("storage.pool.maxLifetime", 1800000));
        hikariConfig.setIdleTimeout(configUtil.getConfig().getInt("storage.pool.idleTimeout", 600000));

        dataSource = new HikariDataSource(hikariConfig);
        createTables();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void createTables() throws SQLException {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
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
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
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
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, uuid);
            ps.setString(2, effect);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    public void saveSelectedAbility(String uuid, String ability) throws SQLException {
        String query = "INSERT INTO fungun_users (uuid, selected_ability) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE selected_ability = VALUES(selected_ability)";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, uuid);
            ps.setString(2, ability);
            ps.executeUpdate();
        }
    }

    public String getSelectedAbility(String uuid) throws SQLException {
        String query = "SELECT selected_ability FROM fungun_users WHERE uuid = ?";
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
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
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, uuid);
            ps.setString(2, ability);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}

