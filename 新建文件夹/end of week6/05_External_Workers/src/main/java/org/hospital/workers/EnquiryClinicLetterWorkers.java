package org.hospital.workers;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class EnquiryClinicLetterWorkers {

    public static final String SEND_CLINIC_LETTER = "send-clinic-letter";

    private EnquiryClinicLetterWorkers() {}

    public static void register(ZeebeClient client, List<JobWorker> workers) {
        workers.add(WorkerSupport.open(client, SEND_CLINIC_LETTER, EnquiryClinicLetterWorkers::sendClinicLetter));
    }

    public static Map<String, Object> sendClinicLetter(Map<String, Object> input) {
        boolean approved = WorkerSupport.bool(input.get("clinicLetterApproved"), false);
        if (!approved) {
            throw new IllegalArgumentException(
                    "Clinic Letter cannot be distributed because clinicLetterApproved is not true.");
        }

        Map<String, Object> out = WorkerSupport.output();
        out.put("clinicLetterSent", true);
        out.put("clinicLetterDistributionReference", WorkerSupport.shortId("CL"));
        out.put("clinicLetterDistributionDate", LocalDate.now().toString());

        return out;
    }
}
