package com.moaaz.migration.processpr;

import com.moaaz.migration.util.CheckSumCalculator;
import com.moaaz.migration.db.MigrationDatabase;
import com.moaaz.migration.exception.MigrationException;
import com.moaaz.migration.model.Migration;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class Migrator {

    private final String migrationsPath;
    private final MigrationDatabase dbAdaptor;

    public Migrator(Connection connection, String migrationsPath) {
        this.migrationsPath = migrationsPath;
        this.dbAdaptor = new MigrationDatabase(connection);
    }

    public void migrate() {
        try {
            List<Migration> migrations = getMigrationFiles();// getting all the files from resources
            for (Migration migration : migrations) {
                processMigration(migration);
            }
        } catch (Exception e) {
            throw new MigrationException("Migration failed", e);
        }
    }

    public List<Migration> getMigrationFiles() throws IOException, URISyntaxException {
        // Get the resource URL and convert to Path
        URL resourceUrl = getClass().getClassLoader().getResource(migrationsPath);
        if (resourceUrl == null) {
            throw new IOException("Migrations directory not found: " + migrationsPath);
        }

        Path path = Paths.get(resourceUrl.toURI());

        return Files.walk(path)
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".sql"))
                .map(this::toMigration)
                .collect(Collectors.toList());
    }

    private Migration toMigration(Path filePath) {
        try {
            String fileName = filePath.getFileName().toString();
            String checksum = CheckSumCalculator.calc(filePath.toString());
            String sqlContent = new String(Files.readAllBytes(filePath));

            Migration migration = new Migration();
            migration.setFileName(fileName);
            migration.setFilePath(filePath.toString());
            migration.setCheckSum(checksum);
            migration.setScript(sqlContent);
            return migration;

        } catch (Exception e) {
            throw new MigrationException("Error processing migration file: " + filePath, e);
        }
    }

    private void processMigration(Migration migration) {
        try {
            Migration existingMigration = dbAdaptor.findMigrationByPath(migration.getFilePath());

            if (existingMigration != null) {
                verifyChecksum(existingMigration, migration);
                return;
            }

            executeNewMigration(migration);

        } catch (SQLException e) {
            throw new MigrationException("Database error processing migration: " + migration.getFileName(), e);
        }
    }

    private void verifyChecksum(Migration dbRecord, Migration fileRecord) {
        if (!dbRecord.getCheckSum().equals(fileRecord.getCheckSum())) {
            throw new MigrationException(
                    String.format("Migration %s has been modified! Expected checksum: %s, Actual: %s",
                            fileRecord.getFileName(),
                            dbRecord.getCheckSum(),
                            fileRecord.getCheckSum())
            );
        }
    }

    private void executeNewMigration(Migration migration) throws SQLException {
        try {
            dbAdaptor.beginTransaction();
            dbAdaptor.executeMigration(migration.getScript());

            migration.setExecuted(true);
            migration.setSuccess(true);
            dbAdaptor.recordMigration(migration);

            dbAdaptor.commitTransaction();
        } catch (Exception e) {
            dbAdaptor.rollbackTransaction();
            throw e;
        }
    }
}