package com.dentalclinic.util;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseUtil {
    private static final String DB_URL = "jdbc:sqlite:dental_clinic.db";
    private static Connection connection;
    private static final Logger LOGGER = Logger.getLogger(DatabaseUtil.class.getName());

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(DB_URL);
                connection.setAutoCommit(true);
                createTablesIfNotExist();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Failed to establish database connection", e);
                throw e;
            }
        }
        return connection;
    }

    private static void createTablesIfNotExist() throws SQLException {
        Statement stmt = null;
        try {
            if (connection == null || connection.isClosed()) {
                throw new SQLException("Database connection is not valid");
            }

            stmt = connection.createStatement();
            
            // Users table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    username TEXT NOT NULL UNIQUE," +
                "    password TEXT NOT NULL," +
                "    role TEXT NOT NULL," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
            
            // Create index on username for faster login queries
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_users_username ON users(username)");

            // Patients table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS patients (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    first_name TEXT NOT NULL," +
                "    last_name TEXT NOT NULL," +
                "    date_of_birth DATE," +
                "    phone TEXT," +
                "    email TEXT," +
                "    address TEXT," +
                "    medical_history TEXT," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
            
            // Create indexes for patient search
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_patients_name ON patients(first_name, last_name)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_patients_phone ON patients(phone)");

            // Appointments table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS appointments (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    patient_id INTEGER," +
                "    practitioner_id INTEGER," +
                "    appointment_date DATETIME NOT NULL," +
                "    duration INTEGER DEFAULT 30," +
                "    status TEXT DEFAULT 'SCHEDULED'," +
                "    notes TEXT," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    FOREIGN KEY (patient_id) REFERENCES patients(id)," +
                "    FOREIGN KEY (practitioner_id) REFERENCES users(id)" +
                ")"
            );
            
            // Create indexes for appointment queries
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_appointments_date ON appointments(appointment_date)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_appointments_patient ON appointments(patient_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_appointments_practitioner ON appointments(practitioner_id)");

            // Medical records table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS medical_records (" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "    patient_id INTEGER," +
                "    practitioner_id INTEGER," +
                "    diagnosis TEXT," +
                "    treatment TEXT," +
                "    notes TEXT," +
                "    visit_date DATE," +
                "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    FOREIGN KEY (patient_id) REFERENCES patients(id)," +
                "    FOREIGN KEY (practitioner_id) REFERENCES users(id)" +
                ")"
            );
            
            // Create indexes for medical records
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_records_patient ON medical_records(patient_id)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_records_date ON medical_records(created_at)");

            // Create default users if no users exist
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users");
            if (rs.next() && rs.getInt("count") == 0) {
                // Create dentist (admin) user
                String insertDentist = "INSERT INTO users (username, password, role) VALUES ('admin', 'password', 'ADMIN')";
                stmt.execute(insertDentist);
                
                // Create secretary user
                String insertSecretary = "INSERT INTO users (username, password, role) VALUES ('secretary', 'password', 'SECRETARY')";
                stmt.execute(insertSecretary);
                
                LOGGER.info("Created default users (dentist and secretary)");
            }
            rs.close();

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create database tables", e);
            throw e;
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Failed to close statement", e);
                }
            }
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error closing database connection", e);
            } finally {
                connection = null;
            }
        }
    }
}
