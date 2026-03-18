package training.cqrstraining.application.dto;

import java.util.List;

public record EnrollmentDto(Long courseId, List<Long> employeeIds) {
}
