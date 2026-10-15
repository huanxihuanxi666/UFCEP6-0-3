package org.hospital.workers;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class FinanceWorkers {

    public static final String PROCESS_PAYMENT = "process-payment";
    public static final String PROCESS_REFUND = "process-refund";

    private static final Set<String> ALLOWED_PAYMENT_SCENARIOS = Set.of(
            "SUCCESSFUL",
            "DECLINED",
            "CANCELLED",
            "INCOMPLETE",
            "CONFIRMATION_LOST",
            "DUPLICATE_RISK"
    );

    private FinanceWorkers() {}

    public static void register(ZeebeClient client, List<JobWorker> workers) {
        workers.add(WorkerSupport.open(client, PROCESS_PAYMENT, FinanceWorkers::processPayment));
        workers.add(WorkerSupport.open(client, PROCESS_REFUND, FinanceWorkers::processRefund));
    }

    public static Map<String, Object> processPayment(Map<String, Object> input) {
        Map<String, Object> out = WorkerSupport.output();

        String scenario = WorkerSupport.str(input.get("paymentScenario"), "SUCCESSFUL")
                .toUpperCase(Locale.ROOT);

        if (!ALLOWED_PAYMENT_SCENARIOS.contains(scenario)) {
            throw new IllegalArgumentException(
                    "Unsupported paymentScenario: " + scenario
                    + ". Expected SUCCESSFUL, DECLINED, CANCELLED, INCOMPLETE, CONFIRMATION_LOST or DUPLICATE_RISK.");
        }

        out.put("paymentStatus", scenario);
        out.put("paymentDate", LocalDate.now().toString());
        out.put("paymentProvider", "SIMULATED_EXTERNAL_PAYMENT_SERVICE");

        String approvedAmount = WorkerSupport.str(input.get("approvedAmount"), "");
        if (!approvedAmount.isEmpty()) {
            out.put("paymentAmount", approvedAmount);
        }

        if ("SUCCESSFUL".equals(scenario)) {
            out.put("transactionReference", WorkerSupport.shortId("PAY"));
            out.put("paymentInvestigationRequired", false);
        } else if ("CONFIRMATION_LOST".equals(scenario) || "DUPLICATE_RISK".equals(scenario)) {
            out.put("paymentInvestigationRequired", true);
        } else {
            out.put("paymentInvestigationRequired", false);
        }

        return out;
    }

    public static Map<String, Object> processRefund(Map<String, Object> input) {
        Map<String, Object> out = WorkerSupport.output();

        boolean refundApproved = WorkerSupport.bool(input.get("refundApproved"), false);
        boolean refundRequired = WorkerSupport.bool(input.get("refundRequired"), refundApproved);

        if (!refundRequired) {
            out.put("refundStatus", "NOT_REQUIRED");
            return out;
        }

        if (!refundApproved) {
            out.put("refundStatus", "NOT_APPROVED");
            return out;
        }

        out.put("refundStatus", "APPROVED_AND_SENT");
        out.put("refundReference", WorkerSupport.shortId("REF"));
        out.put("refundDate", LocalDate.now().toString());

        String refundAmount = WorkerSupport.str(input.get("refundAmount"), "");
        if (!refundAmount.isEmpty()) {
            out.put("processedRefundAmount", refundAmount);
        }

        return out;
    }
}
