package org.hospital.workers;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;

import java.util.List;

/**
 * Corresponds to:
 * Referral_Consultant_Review/BPMN/Referral_Consultant_Review.bpmn
 *
 * This BPMN currently contains User Tasks, Gateways, Send/Receive Tasks and
 * Message Flows, but no BPMN Service Task with a Zeebe job type.
 *
 * Therefore there is intentionally NO external Job Worker subscription here.
 * The file is kept so every BPMN area has a matching Java source file and the
 * absence of a Referral worker is explicit rather than accidental.
 */
public final class ReferralWorkers {

    private ReferralWorkers() {}

    public static void register(ZeebeClient client, List<JobWorker> workers) {
        System.out.println("[INFO] Referral BPMN has no external Service Task job type; no worker registration required.");
    }
}
