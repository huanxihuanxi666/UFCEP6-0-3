package org.hospital.workers;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WorkerLogicSmokeTest {

    @Test
    void appointmentSlotAvailableCreatesGatewayVariables() {
        Map<String, Object> input = new HashMap<>();
        input.put("requestedSlotAvailable", true);
        input.put("requestedAppointmentDate", LocalDate.now().plusDays(7).toString());

        Map<String, Object> out = AppointmentWorkers.searchAppointmentSlot(input);

        assertEquals(true, out.get("slotAvailable"));
        assertEquals(true, out.get("appointmentWithinTwoWeeks"));
        assertNotNull(out.get("appointmentDate"));
    }

    @Test
    void appointmentNoSlotDoesNotRequireDate() {
        Map<String, Object> input = new HashMap<>();
        input.put("requestedSlotAvailable", false);

        Map<String, Object> out = AppointmentWorkers.searchAppointmentSlot(input);

        assertEquals(false, out.get("slotAvailable"));
        assertEquals(false, out.get("appointmentWithinTwoWeeks"));
    }

    @Test
    void correctedAppointmentRetryCanFindSlot() {
        Map<String, Object> input = new HashMap<>();
        input.put("requestedSlotAvailable", false);
        assertEquals(false, AppointmentWorkers.searchAppointmentSlot(input).get("slotAvailable"));

        input.put("requestedSlotAvailable", true);
        assertEquals(true, AppointmentWorkers.searchAppointmentSlot(input).get("slotAvailable"));
    }

    @Test
    void treatmentUnavailableReturnsFalse() {
        Map<String, Object> input = new HashMap<>();
        input.put("requestedTreatmentAvailable", false);

        Map<String, Object> out = TreatmentWorkers.checkTreatmentResource(input);

        assertEquals(false, out.get("treatmentAvailable"));
    }

    @Test
    void correctedResourceRetryCanBecomeAvailable() {
        Map<String, Object> input = new HashMap<>();
        input.put("requestedTreatmentAvailable", false);
        assertEquals(false, TreatmentWorkers.checkTreatmentResource(input).get("treatmentAvailable"));

        input.put("requestedTreatmentAvailable", true);
        assertEquals(true, TreatmentWorkers.checkTreatmentResource(input).get("treatmentAvailable"));
    }

    @Test
    void successfulPaymentReturnsTransactionReference() {
        Map<String, Object> input = new HashMap<>();
        input.put("paymentScenario", "SUCCESSFUL");

        Map<String, Object> out = FinanceWorkers.processPayment(input);

        assertEquals("SUCCESSFUL", out.get("paymentStatus"));
        assertNotNull(out.get("transactionReference"));
        assertEquals(false, out.get("paymentInvestigationRequired"));
    }

    @Test
    void confirmationLostIsInvestigatedNotRecharged() {
        Map<String, Object> input = new HashMap<>();
        input.put("paymentScenario", "CONFIRMATION_LOST");

        Map<String, Object> out = FinanceWorkers.processPayment(input);

        assertEquals("CONFIRMATION_LOST", out.get("paymentStatus"));
        assertEquals(true, out.get("paymentInvestigationRequired"));
        assertFalse(out.containsKey("transactionReference"));
    }

    @Test
    void correctedPaymentRetryCanCompleteWithoutDuplicateRisk() {
        Map<String, Object> input = new HashMap<>();
        input.put("paymentScenario", "DECLINED");
        assertEquals("DECLINED", FinanceWorkers.processPayment(input).get("paymentStatus"));

        input.put("paymentScenario", "SUCCESSFUL");
        Map<String, Object> out = FinanceWorkers.processPayment(input);
        assertEquals("SUCCESSFUL", out.get("paymentStatus"));
        assertNotNull(out.get("transactionReference"));
    }

    @Test
    void approvedRefundProducesReference() {
        Map<String, Object> input = new HashMap<>();
        input.put("refundRequired", true);
        input.put("refundApproved", true);

        Map<String, Object> out = FinanceWorkers.processRefund(input);

        assertEquals("APPROVED_AND_SENT", out.get("refundStatus"));
        assertNotNull(out.get("refundReference"));
    }

    @Test
    void clinicLetterMustBeApproved() {
        Map<String, Object> input = new HashMap<>();
        input.put("clinicLetterApproved", false);

        assertThrows(IllegalArgumentException.class,
                () -> EnquiryClinicLetterWorkers.sendClinicLetter(input));
    }

    @Test
    void approvedClinicLetterSendsSuccessfully() {
        Map<String, Object> input = new HashMap<>();
        input.put("clinicLetterApproved", true);

        Map<String, Object> out = EnquiryClinicLetterWorkers.sendClinicLetter(input);

        assertEquals(true, out.get("clinicLetterSent"));
        assertNotNull(out.get("clinicLetterDistributionReference"));
    }
}
