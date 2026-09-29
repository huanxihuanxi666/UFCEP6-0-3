package org.hospital.workers;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.client.api.worker.JobWorker;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public final class WorkerSupport {

    private WorkerSupport() {}

    public static JobWorker open(
            ZeebeClient client,
            String jobType,
            Function<Map<String, Object>, Map<String, Object>> handler) {

        return client.newWorker()
                .jobType(jobType)
                .handler((jobClient, job) -> complete(jobClient, job, jobType, handler))
                .name("hospital-demo-" + jobType)
                .timeout(Duration.ofSeconds(30))
                .open();
    }



    private static void complete(
            JobClient jobClient,
            ActivatedJob job,
            String jobType,
            Function<Map<String, Object>, Map<String, Object>> handler) {

        try {
            Map<String, Object> input = job.getVariablesAsMap();
            Map<String, Object> output = handler.apply(input);

            jobClient.newCompleteCommand(job)
                    .variables(output)
                    .send()
                    .join();

            System.out.println("[COMPLETED] " + jobType
                    + " key=" + job.getKey()
                    + " output=" + output);

        } catch (Exception exception) {
            String message = exception.getMessage() == null
                    ? exception.getClass().getSimpleName()
                    : exception.getMessage();

            jobClient.newFailCommand(job)
                    .retries(Math.max(0, job.getRetries() - 1))
                    .errorMessage(message)
                    .send()
                    .join();

            System.err.println("[FAILED] " + jobType
                    + " key=" + job.getKey()
                    + " error=" + message);
        }
    }

    public static boolean bool(Object value, boolean defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Boolean b) return b;
        String s = String.valueOf(value).trim();
        if (s.isEmpty()) return defaultValue;
        return Boolean.parseBoolean(s);
    }

    public static String str(Object value, String defaultValue) {
        if (value == null) return defaultValue;
        String s = String.valueOf(value).trim();
        return s.isEmpty() ? defaultValue : s;
    }

    public static LocalDate parseDate(Object value) {
        String raw = str(value, "");
        if (raw.isEmpty()) return null;

        String datePart = raw.length() >= 10 ? raw.substring(0, 10) : raw;
        try {
            return LocalDate.parse(datePart);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    public static String shortId(String prefix) {
        return prefix + "-" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
    }

    public static Map<String, Object> output() {
        return new HashMap<>();
    }
}
