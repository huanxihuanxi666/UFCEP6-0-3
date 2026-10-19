package org.hospital.workers;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class TreatmentWorkers {

    public static final String CHECK_RESOURCE = "check-treatment-resource";

    private TreatmentWorkers() {}

    public static void register(ZeebeClient client, List<JobWorker> workers) {
        workers.add(WorkerSupport.open(client, CHECK_RESOURCE, TreatmentWorkers::checkTreatmentResource));
    }

    public static Map<String, Object> checkTreatmentResource(Map<String, Object> input) {
        Map<String, Object> out = WorkerSupport.output();

        boolean available = WorkerSupport.bool(input.get("requestedTreatmentAvailable"), true);

        out.put("treatmentAvailable", available);
        out.put("resourceCheckReference", WorkerSupport.shortId("RES"));
        out.put("resourceCheckDate", LocalDate.now().toString());
        out.put("resourceCheckStatus", available ? "AVAILABLE" : "TEMPORARILY_UNAVAILABLE");

        return out;
    }
}
