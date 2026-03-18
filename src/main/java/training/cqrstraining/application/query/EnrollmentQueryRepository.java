package training.cqrstraining.application.query;

import training.cqrstraining.application.dto.CourseEnrollmentCountDto;

import java.util.List;

public interface EnrollmentQueryRepository {

    List<CourseEnrollmentCountDto> countEnrollmentsByCourse();
}

