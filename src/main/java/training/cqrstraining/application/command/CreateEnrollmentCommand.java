package training.cqrstraining.application.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.List;

public record CreateEnrollmentCommand(
        @NotNull @Positive Long courseId,
        @NotNull @Size(min = 1) List<@NotNull @Positive Long> employeeIds
) {

    public CreateEnrollmentCommand {
        if (employeeIds != null) {
            HashSet<Long> unique = new HashSet<>();
            for (Long employeeId : employeeIds) {
                if (employeeId != null && !unique.add(employeeId)) {
                    throw new IllegalArgumentException("Employee IDs must be unique in one request.");
                }
            }
        }
    }
}
