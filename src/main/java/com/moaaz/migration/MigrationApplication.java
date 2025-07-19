package com.moaaz.migration;

import com.moaaz.migration.processpr.Migrator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MigrationApplication {
    public static void main(String[] args) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/migration", "root", "root");

        Migrator migrator = new Migrator(connection, "db/migration");
        migrator.migrate();
        System.out.println("Migrated Successfully");

    }
}
