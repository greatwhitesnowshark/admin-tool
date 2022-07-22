/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Database {

    private static HikariDataSource pConnection;

    static {
        init(Config.DB_HOST, Config.DB_PORT, Config.DB_SCHEMA, Config.DB_USER, Config.DB_PASS);
    }

    private static void init(String host, String port, String schema, String username, String password) {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:mariadb://" + host + ":" + port + "/" + schema);
        config.setUsername(username);
        config.setPassword(password);

        config.setMaximumPoolSize(20);
        config.setAutoCommit(true);
        config.setLeakDetectionThreshold(60000);
        config.setConnectionTestQuery("SELECT 1");
        //hikari.setIdleTimeout(30000);
        //hikari.setMaxLifetime(1800000);

        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("autoReconnect", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("useServerPrepStmts", "false");
        //hikari.addDataSourceProperty("tcpNoDelay", "true");
        //hikari.addDataSourceProperty("tcpKeepAlive", "true");

        pConnection = new HikariDataSource(config);
    }

    public static Connection connection() {
        if (pConnection == null) {
            return null;
        }
        try {
            return pConnection.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static int execute(Connection con, PreparedStatement propSet, Object... command) throws SQLException {
        return DBHelper.execute(con, propSet, command);
    }

    public static int softExecute(PreparedStatement propSet, Object... command) throws SQLException {
        return DBHelper.softExecute(propSet, command);
    }

    public static void close() {
        pConnection.close();
    }
}
