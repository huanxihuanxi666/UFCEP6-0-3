package org.hospital.workers;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.worker.JobWorker;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

public final class AppointmentWorkers {

    public static final String SEARCH_SLOT = "search-appointment-slot";
    public static final String SEND_APPOINTMENT_LETTER = "send-appointment-letter";

    private AppointmentWorkers() {}

    public static void register(ZeebeClient client, List<JobWorker> workers) {
        workers.add(WorkerSupport.open(client, SEARCH_SLOT, AppointmentWorkers::searchAppointmentSlot));
        workers.add(WorkerSupport.open(client, SEND_APPOINTMENT_LETTER, AppointmentWorkers::sendAppointmentLetter));
    }

    public static Map<String, Object> searchAppointmentSlot(Map<String, Object> input) {
        Map<String, Object> out = WorkerSupport.output();

        boolean available = WorkerSupport.bool(input.get("requestedSlotAvailable"), true);
        out.put("slotAvailable", available);

        if (!available) {
            out.put("appointmentWithinTwoWeeks", false);
            out.put("schedulingStatus", "NO_SUITABLE_SLOT");
            return out;
        }

        LocalDate appointmentDate = WorkerSupport.parseDate(input.get("requestedAppointmentDate"));
        if (appointmentDate == null) {
            appointmentDate = LocalDate.now().plusDays(10);
        }

        long days = ChronoUnit.DAYS.between(LocalDate.now(), appointmentDate);
        boolean withinTwoWeeks = days >= 0 && days <= 14;

        out.put("appointmentDate", appointmentDate.toString());
        out.put("appointmentWithinTwoWeeks", withinTwoWeeks);
        out.put("schedulingStatus", "SLOT_FOUND");
        out.put("schedulingReference", WorkerSupport.shortId("SCH"));

        return out;
    }

    public static Map<String, Object> sendAppointmentLetter(Map<String, Object> input) {
        Map<String, Object> out = WorkerSupport.output();

        out.put("appointmentLetterSent", true);
        out.put("appointmentCorrespondenceReference", WorkerSupport.shortId("APT"));
        out.put("appointmentLetterSentDate", LocalDate.now().toString());

        return out;
    }
}
