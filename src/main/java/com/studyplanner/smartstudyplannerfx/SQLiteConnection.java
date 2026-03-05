package com.studyplanner.smartstudyplannerfx;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.sqlite.SQLiteConfig;

public class SQLiteConnection {
    private static final String URL = "jdbc:sqlite:study_db.db";
    private static Connection sharedConnection = null;

    public static Connection getConnection() throws SQLException {
        if (sharedConnection == null || sharedConnection.isClosed()) {
            SQLiteConfig config = new SQLiteConfig();
            config.enforceForeignKeys(true); 
            sharedConnection = DriverManager.getConnection(URL, config.toProperties());
        }
        return sharedConnection;
    }

    public static void initialize() {
        try (Statement stmt = getConnection().createStatement()) {
            
            // 1. Students Table
            stmt.execute("CREATE TABLE IF NOT EXISTS students (" +
                    "student_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, email TEXT NOT NULL UNIQUE, password TEXT NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 2. Subjects Table
            stmt.execute("CREATE TABLE IF NOT EXISTS subjects (" +
                    "subject_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "subject_name TEXT NOT NULL, description TEXT)");

            // 3. Syllabus Table
            stmt.execute("CREATE TABLE IF NOT EXISTS syllabus (" +
                    "topic_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "subject_id INTEGER NOT NULL, parent_topic_id INTEGER DEFAULT NULL, " +
                    "topic_name TEXT NOT NULL, difficulty_level TEXT DEFAULT 'Medium', " +
                    "FOREIGN KEY(subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE)");

            // 4. Performance Table (Current Status)
            stmt.execute("CREATE TABLE IF NOT EXISTS performance (" +
                    "student_id INTEGER NOT NULL, subject_id INTEGER, " +
                    "topic_id INTEGER NOT NULL, score INTEGER NOT NULL, " +
                    "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "PRIMARY KEY (student_id, topic_id), " +
                    "FOREIGN KEY(student_id) REFERENCES students(student_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(topic_id) REFERENCES syllabus(topic_id) ON DELETE CASCADE)");

            // 5. Performance History Table (Includes current and previous score comparison)
            stmt.execute("CREATE TABLE IF NOT EXISTS performance_history (" +
                    "history_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "student_id INTEGER NOT NULL, " +
                    "subject_id INTEGER NOT NULL, " +
                    "topic_id INTEGER NOT NULL, " +
                    "current_score INTEGER NOT NULL, " +
                    "previous_score INTEGER, " + 
                    "recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(student_id) REFERENCES students(student_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(topic_id) REFERENCES syllabus(topic_id) ON DELETE CASCADE)");

            // 6. Performance Log Table (Simple audit trail)
            stmt.execute("CREATE TABLE IF NOT EXISTS performance_log (" +
                    "log_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "student_id INTEGER NOT NULL, " +
                    "subject_id INTEGER NOT NULL, " +
                    "topic_id INTEGER NOT NULL, " +
                    "score INTEGER NOT NULL, " +
                    "recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(student_id) REFERENCES students(student_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(topic_id) REFERENCES syllabus(topic_id) ON DELETE CASCADE)");

            // 7. Prerequisites Table
            stmt.execute("CREATE TABLE IF NOT EXISTS prerequisites (" +
                    "subject_id INTEGER NOT NULL, prerequisite_id INTEGER NOT NULL, " +
                    "PRIMARY KEY (subject_id, prerequisite_id), " +
                    "FOREIGN KEY(subject_id) REFERENCES subjects(subject_id) ON DELETE CASCADE)");

            System.out.println("SQLite Database initialized successfully with Performance History and Logs!");
        } catch (SQLException e) {
            System.err.println("Database setup failed: " + e.getMessage());
        }
    }

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