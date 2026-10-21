package org.hospital.workers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ApplicationDataRepositoryTest {

    @Test
    void savesAndRetrievesAnApplicationRecordByPatientId() {
        ApplicationDataRepository repository = new ApplicationDataRepository(
                "jdbc:h2:mem:application-data-test;DB_CLOSE_DELAY=-1");

        repository.save("P-1001", "test", Map.of(
                "referralId", "R-44",
                "fundingType", "INSURER",
                "appointmentDate", "2026-10-14"));

        Map<String, Object> result = repository.load("P-1001");
        assertEquals("R-44", result.get("referralId"));
        assertEquals("INSURER", result.get("fundingType"));
        assertEquals("2026-10-14", result.get("appointmentDate"));
    }

    @Test
    void fileDatabaseKeepsDataAfterRepositoryIsReopened() throws Exception {
        Path database = Files.createTempDirectory("hospital-persistence-")
                .resolve("patient-record");
        String jdbcUrl = "jdbc:h2:file:" + database.toAbsolutePath();

        new ApplicationDataRepository(jdbcUrl).save("P-2002", "first-run", Map.of(
                "patientName", "Persistent Patient",
                "referralStatus", "NEW"));

        // A new repository object simulates closing and reopening the worker.
        Map<String, Object> reopenedRecord = new ApplicationDataRepository(jdbcUrl)
                .load("P-2002");
        assertEquals("Persistent Patient", reopenedRecord.get("patientName"));
        assertEquals("NEW", reopenedRecord.get("referralStatus"));
    }
}
