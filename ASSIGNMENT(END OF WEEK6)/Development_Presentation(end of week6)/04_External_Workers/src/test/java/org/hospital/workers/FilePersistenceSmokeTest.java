package org.hospital.workers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Runnable proof that the H2 file database survives a worker restart.
 * Run with the H2 driver on the classpath after compiling this project.
 */
public final class FilePersistenceSmokeTest {
    private FilePersistenceSmokeTest() {}

    public static void main(String[] args) throws Exception {
        Path database = Files.createTempDirectory("hospital-persistence-")
                .resolve("patient-record");
        String jdbcUrl = "jdbc:h2:file:" + database.toAbsolutePath();

        new ApplicationDataRepository(jdbcUrl).save("P-2002", "first-run", Map.of(
                "patientName", "Persistent Patient",
                "referralStatus", "NEW"));

        Map<String, Object> restored = new ApplicationDataRepository(jdbcUrl)
                .load("P-2002");
        if (!"Persistent Patient".equals(restored.get("patientName"))
                || !"NEW".equals(restored.get("referralStatus"))) {
            throw new AssertionError("Persisted values were not restored: " + restored);
        }

        System.out.println("PERSISTENCE_SMOKE_TEST_PASSED database=" + database + ".mv.db");
    }
}
