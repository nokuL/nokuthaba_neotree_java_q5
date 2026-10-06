import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FollowUpEligibilityTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 6);

    @Test
    void dischargedLivingPatientWithValidPastDateIsDue() {
        Patient p = new Patient("P001", "discharged", "2026-09-15", "recovered");

        assertEquals(FollowUpEligibility.Status.DUE, p.followUpEligibility(TODAY).status());
        assertTrue(p.isFollowUpDue(TODAY));
    }

    @Test
    void notDischargedOrDeceasedPatientsAreNotDue() {
        Patient admitted = new Patient("P002", "admitted", "2026-09-15", "recovered");
        Patient deceased = new Patient("P003", "discharged", "2026-09-15", "deceased");

        assertEquals(FollowUpEligibility.Status.NOT_DUE, admitted.followUpEligibility(TODAY).status());
        assertEquals(FollowUpEligibility.Status.NOT_DUE, deceased.followUpEligibility(TODAY).status());
        assertFalse(admitted.isFollowUpDue(TODAY));
        assertFalse(deceased.isFollowUpDue(TODAY));
    }

    @Test
    void badDischargeDatesCannotBeAssessedAndDoNotCrash() {
        String[] badDates = {null, "", "15/09/2026", "2026-02-30", "2026-10-07"};

        for (String date : badDates) {
            Patient p = new Patient("P004", "discharged", date, "recovered");
            FollowUpEligibility.Result result = assertDoesNotThrow(() -> p.followUpEligibility(TODAY));

            assertEquals(FollowUpEligibility.Status.CANNOT_ASSESS, result.status(), "date: " + date);
            assertFalse(p.isFollowUpDue(TODAY));
        }
    }
}
