import java.time.LocalDate;

public class Patient {
    private final String id;
    private final String status;
    private final String dischargeDate;
    private final String outcome;

    public Patient(String id, String status, String dischargeDate, String outcome) {
        this.id = id;
        this.status = status;
        this.dischargeDate = dischargeDate;
        this.outcome = outcome;
    }

    public String getId() { return id; }
    public String getStatus() { return status; }
    public String getDischargeDate() { return dischargeDate; }
    public String getOutcome() { return outcome; }

    public FollowUpEligibility.Result followUpEligibility(LocalDate today) {
        return FollowUpEligibility.assess(this, today);
    }

    public boolean isFollowUpDue(LocalDate today) {
        return followUpEligibility(today).status() == FollowUpEligibility.Status.DUE;
    }

    public boolean isFollowUpDue() {
        return isFollowUpDue(LocalDate.now());
    }

    @Override
    public String toString() {
        return "Patient[" + id + ", " + status + ", " + dischargeDate + ", " + outcome + "]";
    }
}
