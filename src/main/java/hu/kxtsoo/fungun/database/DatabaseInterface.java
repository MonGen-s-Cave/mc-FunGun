package hu.kxtsoo.fungun.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseInterface {
    void initialize() throws SQLException;

    void createTables() throws SQLException;

    Connection getConnection() throws SQLException;

    void saveSelectedEffect(String uuid, String effect) throws SQLException;
    String getSelectedEffect(String uuid) throws SQLException;
    boolean isEffectSelected(String uuid, String effect) throws SQLException;

    void saveSelectedAbility(String uuid, String ability) throws SQLException;
    String getSelectedAbility(String uuid) throws SQLException;
    boolean isAbilitySelected(String uuid, String ability) throws SQLException;

    void close() throws SQLException;
}