package com.studyplanner.smartstudyplannerfx;

import java.sql.*;
import org.sqlite.SQLiteConfig;

public class SQLiteConnection {
    // Creates a local file named 'study_db.db' in your project root
    private static final String URL = "jdbc:sqlite:study_db.db";

    private static Connection sharedConnection = null;

    public static Connection getConnection() throws SQLException {

        if (sharedConnection == null || sharedConnection.isClosed()) {
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true); // SQLite requires this to enforce relationships
            sharedConnection = DriverManager.getConnection(URL, config.toProperties());
        }
        return sharedConnection; // Returns the exact same connection every time
    }

    // Builds your tables automatically if they don't exist
    public static void initialize() {

        // auto-close
        try (Statement stmt = getConnection().createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS students (" +
                    "student_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, email TEXT NOT NULL UNIQUE, password TEXT NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            stmt.execute("CREATE TABLE IF NOT EXISTS subjects (" +
                    "subject_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "subject_name TEXT NOT NULL, description TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS syllabus (" +
                    "topic_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "subject_id INTEGER NOT NULL, parent_topic_id INTEGER DEFAULT NULL, " +
                    "topic_name TEXT NOT NULL, difficulty_level TEXT DEFAULT 'Medium', " +
                    "FOREIGN KEY(subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS performance (" +
                    "student_id INTEGER NOT NULL, subject_id INTEGER, " +
                    "topic_id INTEGER NOT NULL, score INTEGER NOT NULL, " +
                    "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "PRIMARY KEY (student_id, topic_id), " +
                    "FOREIGN KEY(student_id) REFERENCES students(student_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(topic_id) REFERENCES syllabus(topic_id) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS performance_history (" +
                    "history_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "student_id INTEGER NOT NULL, " +
                    "subject_id INTEGER NOT NULL, " +
                    "topic_id INTEGER NOT NULL, " +
                    "current_score INTEGER NOT NULL, " +
                    "previous_score INTEGER, " +
                    "recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(student_id) REFERENCES students(student_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(topic_id) REFERENCES syllabus(topic_id) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS performance_log (" +
                    "log_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "student_id INTEGER NOT NULL, " +
                    "subject_id INTEGER NOT NULL, " +
                    "topic_id INTEGER NOT NULL, " +
                    "score INTEGER NOT NULL, " +
                    "recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(student_id) REFERENCES students(student_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(topic_id) REFERENCES syllabus(topic_id) ON DELETE CASCADE)");

            stmt.execute("CREATE TABLE IF NOT EXISTS prerequisites (" +
                    "subject_id INTEGER NOT NULL, prerequisite_id INTEGER NOT NULL, " +
                    "PRIMARY KEY (subject_id, prerequisite_id), " +
                    "FOREIGN KEY(subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE)");

            System.out.println("SQLite Database initialized successfully!");
        } catch (SQLException e) {
            System.err.println("Database setup failed: " + e.getMessage());
        }
    }

    // Safely handles the application closing
    public static void shutdown() {
        try {
            if (sharedConnection != null && !sharedConnection.isClosed()) {
                sharedConnection.close();
            }
            System.out.println("Smart Study Planner database connection closed.");
        } catch (SQLException e) {
            System.err.println("Error closing database: " + e.getMessage());
        }
    }
}