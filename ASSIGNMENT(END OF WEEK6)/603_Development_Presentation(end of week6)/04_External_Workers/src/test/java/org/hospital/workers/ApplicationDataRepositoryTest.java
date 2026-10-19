package org.hospital.workers;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
