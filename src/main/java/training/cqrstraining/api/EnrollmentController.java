package training.cqrstraining.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import training.cqrstraining.application.command.CreateCancellationCommand;
import training.cqrstraining.application.command.CreateEnrollmentCommand;
import training.cqrstraining.application.dto.CourseEnrollmentCountDto;
import training.cqrstraining.application.dto.EnrollmentDto;
import training.cqrstraining.application.service.EnrollmentApplicationService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EnrollmentController {

    private final EnrollmentApplicationService enrollmentService;

    public EnrollmentController(EnrollmentApplicationService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/enrollments")
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentDto enroll(@Valid @RequestBody CreateEnrollmentCommand command) {
        return enrollmentService.enroll(command);
    }

    @PostMapping("/courses/{courseId}/cancellations")
    public EnrollmentDto cancel(
            @PathVariable @Positive Long courseId,
            @Valid @RequestBody CreateCancellationRequest request
    ) {
        return enrollmentService.cancel(new CreateCancellationCommand(courseId, request.employeeIds()));
    }

    @GetMapping("/enrollments")
    public List<EnrollmentDto> listAll() {
        return enrollmentService.listAll();
    }

    @GetMapping("/courses/{courseId}/enrollments")
    public EnrollmentDto listByCourse(@PathVariable @Positive Long courseId) {
        return enrollmentService.listByCourse(courseId);
    }

    @GetMapping("/courses/enrollment-counts")
    public List<CourseEnrollmentCountDto> getEnrollmentCounts() {
        return enrollmentService.countEnrollmentsByCourse();
    }
}
