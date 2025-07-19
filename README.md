SQLMigrate 🚀
A lightweight, SQL-native database migration tool

SQLMigrate is a simple yet powerful tool for managing database schema changes using plain SQL files. Inspired by tools like Liquibase and Flyway, it focuses on native SQL migrations with version tracking, checksum validation, and rollback support.

Features
✔ Plain SQL migrations – No DSL, just .sql files
✔ Checksum validation – Prevents modified migrations after execution
✔ Atomic transactions – Each migration runs in a transaction (rollback on failure)
✔ Version tracking – Tracks executed migrations in a schema history table
✔ Simple setup – Just place SQL files in db/migration
