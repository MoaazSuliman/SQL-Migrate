package com.moaaz.migration.db;

import com.moaaz.migration.model.Migration;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MigrationDatabase {

    private final Connection connection;

    public MigrationDatabase(Connection connection) {
        this.connection = connection;
        createMigrationTableIfNotExists();
    }

    private void createMigrationTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS schema_migrations (\n" +
                "    file_name VARCHAR(255) NOT NULL,\n" +
                "    file_path VARCHAR(512) NOT NULL,\n" +
                "    script TEXT,\n" +
                "    checksum VARCHAR(64) NOT NULL," +
                "    \n" +
                "    executed_at TIMESTAMP,\n" +
                "    executed BOOLEAN NOT NULL DEFAULT FALSE,\n" +
                "    success BOOLEAN NOT NULL DEFAULT FALSE,\n" +
                "    \n" +
                "    PRIMARY KEY (file_path),\n" +
                "    CONSTRAINT checksum_unique UNIQUE (checksum)\n" +
                ");";
        executeUpdate(sql);
    }

    public void executeUpdate(String sql) {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute SQL update: " + e.getMessage(), e);
        }
    }

    public List<Migration> getAppliedMigrations() {
        String sql = "SELECT file_name, file_path, script, checksum, executed_at, executed, success " +
                "FROM schema_migrations WHERE executed = TRUE ORDER BY executed_at ASC";

        List<Migration> migrations = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                Migration migration = new Migration();
                migration.setFileName(rs.getString("file_name"));
                migration.setFilePath(rs.getString("file_path"));
                migration.setScript(rs.getString("script"));
                migration.setCheckSum(rs.getString("checksum"));

                Timestamp timestamp = rs.getTimestamp("executed_at");
                if (timestamp != null) {
                    migration.setExecutedAt(timestamp.toInstant());
                }

                migration.setExecuted(rs.getBoolean("executed"));
                migration.setSuccess(rs.getBoolean("success"));

                migrations.add(migration);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch applied migrations: " + e.getMessage(), e);
        }

        return migrations;
    }

    public void recordMigration(Migration migration) {
        String sql = "INSERT INTO schema_migrations " +
                "(file_name, file_path, script, checksum, executed_at, executed, success) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, migration.getFileName());
            ps.setString(2, migration.getFilePath());
            ps.setString(3, migration.getScript());
            ps.setString(4, migration.getCheckSum());

            if (migration.getExecutedAt() != null) {
                ps.setTimestamp(5, Timestamp.from(migration.getExecutedAt()));
            } else {
                ps.setTimestamp(5, Timestamp.from(Instant.now()));
            }

            ps.setBoolean(6, migration.isExecuted());
            ps.setBoolean(7, migration.isSuccess());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to record migration: " + e.getMessage(), e);
        }
    }

    // Additional useful methods
    public boolean isMigrationApplied(String checksum) {
        String sql = "SELECT COUNT(*) FROM schema_migrations WHERE checksum = ? AND executed = TRUE";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, checksum);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check migration status: " + e.getMessage(), e);
        }
        return false;
    }

    public void markMigrationAsFailed(Migration migration) {
        String sql = "UPDATE schema_migrations SET success = FALSE WHERE file_path = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, migration.getFilePath());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to mark migration as failed: " + e.getMessage(), e);
        }
    }

    public void beginTransaction() throws SQLException {
        connection.setAutoCommit(false);
    }

    public void commitTransaction() throws SQLException {
        connection.commit();
        connection.setAutoCommit(true);
    }

    public void rollbackTransaction() {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to rollback transaction", e);
        }
    }

    public void executeMigration(String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public Migration findMigrationByVersion(String version) throws SQLException {
        String sql = "SELECT * FROM schema_migrations WHERE file_name LIKE ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "V" + version + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Migration migration = new Migration();
                    migration.setFileName(rs.getString("file_name"));
                    migration.setFilePath(rs.getString("file_path"));
                    migration.setScript(rs.getString("script"));
                    migration.setCheckSum(rs.getString("checksum"));
                    migration.setExecuted(rs.getBoolean("executed"));
                    migration.setSuccess(rs.getBoolean("success"));
                    return migration;
                }
            }
        }
        return null;
    }

    public Migration findMigrationByPath(String filePath) throws SQLException {
        String sql = "SELECT * FROM schema_migrations WHERE file_path = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, filePath);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Migration migration = new Migration();
                    migration.setFileName(rs.getString("file_name"));
                    migration.setFilePath(rs.getString("file_path"));
                    migration.setScript(rs.getString("script"));
                    migration.setCheckSum(rs.getString("checksum"));
                    migration.setExecuted(rs.getBoolean("executed"));
                    migration.setSuccess(rs.getBoolean("success"));
                    return migration;
                }
            }
        }
        return null;
    }
}