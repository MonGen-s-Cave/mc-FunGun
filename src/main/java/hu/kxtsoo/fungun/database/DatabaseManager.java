package hu.kxtsoo.fungun.database;

import hu.kxtsoo.fungun.database.impl.H2;
import hu.kxtsoo.fungun.database.impl.MySQL;
import hu.kxtsoo.fungun.database.impl.SQLite;
import hu.kxtsoo.fungun.util.ConfigUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {

    private static DatabaseInterface database;

    public static void initialize(ConfigUtil configUtil, JavaPlugin plugin) throws SQLException {
        String driver = configUtil.getConfig().getString("storage.driver", "h2");
        switch (driver.toLowerCase()) {
            case "sqlite":
                database = new SQLite(plugin);
                database.initialize();
                break;
            case "mysql":
                database = new MySQL(configUtil, plugin);
                database.initialize();
                break;
            case "h2":
                database = new H2(plugin);
                database.initialize();
                break;
            default:
                throw new IllegalArgumentException("Unsupported database driver: " + driver);
        }

        database.createTables();
    }

    public static void saveSelectedEffect(String uuid, String effect) throws SQLException {
        database.saveSelectedEffect(uuid, effect);
    }

    public static String getSelectedEffect(String uuid) throws SQLException {
        return database.getSelectedEffect(uuid);
    }

    public static boolean isEffectSelected(String uuid, String effect) throws SQLException {
        return database.isEffectSelected(uuid, effect);
    }

    public static void saveSelectedAbility(String uuid, String ability) throws SQLException {
        database.saveSelectedAbility(uuid, ability);
    }

    public static String getSelectedAbility(String uuid) throws SQLException {
        return database.getSelectedAbility(uuid);
    }

    public static boolean isAbilitySelected(String uuid, String ability) throws SQLException {
        return database.isAbilitySelected(uuid, ability);
    }

    public static Connection getConnection() throws SQLException {
        if (database != null) {
            return database.getConnection();
        }
        throw new SQLException("Database is not initialized.");
    }

    public static void close() throws SQLException {
        if (database != null) {
            database.close();
        }
    }

    public static String getDatabaseType() {
        if (database instanceof MySQL) {
            return "MySQL";
        } else if (database instanceof SQLite) {
            return "SQLite";
        } else if (database instanceof H2) {
            return "H2";
        } else {
            return "Unknown";
        }
    }
}