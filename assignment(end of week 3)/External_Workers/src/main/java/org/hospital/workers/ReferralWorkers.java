package org.hospital.workers;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;
import java.util.List;
import java.util.Map;

/**
 * Corresponds to:
 * Referral_Consultant_Review/BPMN/Referral_Consultant_Review.bpmn
 *
 * The intake service validates the recorded referral reference before the
 * Medical Secretary checks supporting information.
 */
public final class ReferralWorkers {

    public static final String VALIDATE_REFERRAL_REFERENCE = "validate-referral-reference";

    private ReferralWorkers() {}

    public static void register(ZeebeClient client, List<JobWorker> workers) {
        workers.add(WorkerSupport.open(
                client,
                VALIDATE_REFERRAL_REFERENCE,
                ReferralWorkers::validateReferralReference));
    }

    public static Map<String, Object> validateReferralReference(Map<String, Object> input) {
        Map<String, Object> out = WorkerSupport.output();
        boolean valid = WorkerSupport.bool(input.get("requestedReferralReferenceValid"), true);

        out.put("referralReferenceValidated", valid);
        out.put("referralValidationStatus", valid ? "VALID" : "REVIEW_REQUIRED");
        out.put("referralValidationReference", WorkerSupport.shortId("REF"));
        return out;
    }
}
