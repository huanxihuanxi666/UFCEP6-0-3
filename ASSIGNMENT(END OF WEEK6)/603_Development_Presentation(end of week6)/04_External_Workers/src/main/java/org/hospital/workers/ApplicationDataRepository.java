package org.hospital.workers;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Small embedded repository for the Release 1 prototype.
 *
 * <p>The Zeebe variable store only keeps variables for one process instance.
 * This repository stores patient/application values in a durable H2 database so
 * later jobs and later process instances can retrieve the same record by
 * {@code patientId}. It deliberately stores only scalar workflow data, never
 * documents or credentials.</p>
 */
public final class ApplicationDataRepository {

    // A file-backed URL keeps records after the worker is stopped and started again.
    // No AUTO_SERVER mode is used: each worker job opens and closes its connection,
    // so the database file is released cleanly when the application closes.
    private static final String DEFAULT_URL = "jdbc:h2:file:./data/hospital-applications";
    private static final ApplicationDataRepository DEFAULT = new ApplicationDataRepository(DEFAULT_URL);

    private final String jdbcUrl;

    public ApplicationDataRepository(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        initialise();
    }

    public static ApplicationDataRepository defaultRepository() {
        return DEFAULT;
    }

    public static Path defaultDatabasePath() {
        return Path.of("data", "hospital-applications.mv.db").toAbsolutePath();
    }

    public Map<String, Object> load(String patientId) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (patientId == null || patientId.isBlank()) {
            return result;
        }

        String sql = "SELECT data_key, data_value FROM application_data WHERE patient_id = ?";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, patientId.trim());
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    result.put(rows.getString("data_key"), rows.getString("data_value"));
                }
            }
            return result;
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not retrieve persisted application data", exception);
        }
    }

    public void save(String patientId, String source, Map<String, Object> values) {
        if (patientId == null || patientId.isBlank()) {
            return;
        }
        String sql = "MERGE INTO application_data "
                + "(patient_id, data_key, data_value, source, updated_at) KEY(patient_id, data_key) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                if (entry.getKey() == null || entry.getValue() == null || entry.getValue() instanceof Map<?, ?>) {
                    continue;
                }
                statement.setString(1, patientId.trim());
                statement.setString(2, entry.getKey());
                statement.setString(3, String.valueOf(entry.getValue()));
                statement.setString(4, source);
                statement.setString(5, Instant.now().toString());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not persist application data", exception);
        }
    }

    private void initialise() {
        String sql = "CREATE TABLE IF NOT EXISTS application_data ("
                + "patient_id VARCHAR(128) NOT NULL, "
                + "data_key VARCHAR(128) NOT NULL, "
                + "data_value VARCHAR(4000), "
                + "source VARCHAR(128) NOT NULL, "
                + "updated_at VARCHAR(64) NOT NULL, "
                + "PRIMARY KEY (patient_id, data_key))";
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException exception) {
            throw new IllegalStateException("Could not initialise application data repository", exception);
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, "sa", "");
    }
}
