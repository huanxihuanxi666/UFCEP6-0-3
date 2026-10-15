package org.hospital.workers;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;

import java.util.ArrayList;
import java.util.List;

public final class HospitalExternalWorkers {

    private static final String ADDRESS =
            System.getenv().getOrDefault("ZEEBE_ADDRESS", "127.0.0.1:26500");

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

            System.out.println("Workers are ready.");
            System.out.println("Registered worker count: " + workers.size());
            System.out.println("Expected job types:");
            System.out.println(" - validate-referral-reference");
            System.out.println(" - search-appointment-slot");
            System.out.println(" - send-appointment-letter");
            System.out.println(" - check-treatment-resource");
            System.out.println(" - process-payment");
            System.out.println(" - process-refund");
            System.out.println(" - send-clinic-letter");

            Thread.currentThread().join();
        }
    }
}
