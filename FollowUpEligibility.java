import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.regex.Pattern;

public class FollowUpEligibility {

    public enum Status { DUE, NOT_DUE, CANNOT_ASSESS }

    public record Result(Status status, String reason) {}

    private static final Pattern DATE_SHAPE = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    public static Result assess(Patient p, LocalDate today) {
        if (p == null) {
            return new Result(Status.CANNOT_ASSESS, "no patient record");
        }
        if (today == null) {
            return new Result(Status.CANNOT_ASSESS, "no reference date for today");
        }

        String status = clean(p.getStatus());
        if (status == null) {
            return new Result(Status.CANNOT_ASSESS, "status is missing");
        }
        if (!status.equals("discharged")) {
            return new Result(Status.NOT_DUE, "status is '" + p.getStatus() + "', not discharged");
        }

        if ("deceased".equals(clean(p.getOutcome()))) {
            return new Result(Status.NOT_DUE, "patient is deceased");
        }

        String rawDate = p.getDischargeDate() == null ? null : p.getDischargeDate().trim();
        if (rawDate == null || rawDate.isEmpty()) {
            return new Result(Status.CANNOT_ASSESS, "discharge date is missing");
        }
        if (!DATE_SHAPE.matcher(rawDate).matches()) {
            return new Result(Status.CANNOT_ASSESS, "discharge date '" + rawDate + "' is not in YYYY-MM-DD format");
        }
        LocalDate discharged;
        try {
            discharged = LocalDate.parse(rawDate, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return new Result(Status.CANNOT_ASSESS, "discharge date '" + rawDate + "' is not a real date");
        }
        if (discharged.isAfter(today)) {
            return new Result(Status.CANNOT_ASSESS, "discharge date " + rawDate + " is in the future");
        }

        return new Result(Status.DUE, "discharged on " + rawDate);
    }

    private static String clean(String s) {
        if (s == null || s.isBlank()) return null;
        return s.trim().toLowerCase();
    }

    public static void main(String[] args) {
        LocalDate today = LocalDate.now();
        List<Patient> patients = List.of(
                new Patient("P001", "discharged", "2026-09-01", "recovered"),
                new Patient("P002", "admitted", "2026-09-01", "recovered"),
                new Patient("P003", "discharged", "2026-09-01", "deceased"),
                new Patient("P004", "discharged", null, "recovered"),
                new Patient("P005", "discharged", "01/09/2026", "recovered"),
                new Patient("P006", "discharged", "2026-02-30", "recovered"),
                new Patient("P007", "discharged", today.plusDays(3).toString(), "recovered"),
                new Patient("P008", null, "2026-09-01", "recovered")
        );

        for (Patient p : patients) {
            Result r = p.followUpEligibility(today);
            System.out.printf("%-5s %-14s %s%n", p.getId(), r.status(), r.reason());
        }
    }
}
