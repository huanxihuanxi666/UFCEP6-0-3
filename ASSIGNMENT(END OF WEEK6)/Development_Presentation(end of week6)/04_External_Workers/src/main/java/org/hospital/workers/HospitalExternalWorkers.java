package org.hospital.workers;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;

import java.util.ArrayList;
import java.util.List;

public final class HospitalExternalWorkers {

    private static final String ADDRESS =
            System.getenv().getOrDefault("ZEEBE_ADDRESS", "127.0.0.1:26500");
    private static final List<String> EXPECTED_JOB_TYPES = List.of(
            "validate-referral-reference",
            "search-appointment-slot",
            "send-appointment-letter",
            "check-treatment-resource",
            "process-payment",
            "process-refund",
            "send-clinic-letter");

    private HospitalExternalWorkers() {}

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Connecting to Zeebe at " + ADDRESS);

        try (ZeebeClient client = ZeebeClient.newClientBuilder()
                .gatewayAddress(ADDRESS)
                .usePlaintext()
                .build()) {

            List<JobWorker> workers = new ArrayList<>();

            ReferralWorkers.register(client, workers);
            AppointmentWorkers.register(client, workers);
            TreatmentWorkers.register(client, workers);
            FinanceWorkers.register(client, workers);
            EnquiryClinicLetterWorkers.register(client, workers);

            if (workers.size() != EXPECTED_JOB_TYPES.size()) {
                throw new IllegalStateException("Worker registration mismatch: expected "
                        + EXPECTED_JOB_TYPES.size() + " but registered " + workers.size());
            }

            System.out.println("Workers are ready.");
            System.out.println("Registered worker count: " + workers.size());
            System.out.println("Expected job types:");
            EXPECTED_JOB_TYPES.forEach(type -> System.out.println(" - " + type));

            Thread.currentThread().join();
        }
    }
}
