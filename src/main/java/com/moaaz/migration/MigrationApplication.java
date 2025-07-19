package com.moaaz.migration;

import com.moaaz.migration.processpr.Migrator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MigrationApplication {
    public static void main(String[] args) throws SQLException {
        // Setup in-memory database (H2)
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/migration", "root", "root");

        // Create test migrations in src/test/resources/db/migration/
        Migrator migrator = new Migrator(connection, "db/migration");

        // Run migrations
        migrator.migrate();
        System.out.println("Migrated Successfully");

//        if (args.length < 2) {
//            System.out.println("Usage: java -jar migration.jar <jdbc-url> <username> <password>");
//            System.exit(1);
//        }
//
//        String jdbcUrl = args[0];asdasd
//        String username = args[1];
//        String password = args[2];
//        String migrationsPath = "db/migration";
//
//        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
//            Migrator migrator = new Migrator(connection, migrationsPath);
//            migrator.migrate();
//            System.out.println("Migrations completed successfully!");
//        } catch (Exception e) {
//            System.err.println("Migration failed: " + e.getMessage());
//            e.printStackTrace();
//            System.exit(1);
//        }
    }
}
