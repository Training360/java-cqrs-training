package training.cqrstraining.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import training.cqrstraining.application.command.CreateCancellationCommand;
import training.cqrstraining.application.command.CreateEnrollmentCommand;
import training.cqrstraining.application.dto.CourseEnrollmentCountDto;
import training.cqrstraining.application.dto.EnrollmentDto;
import training.cqrstraining.application.service.EnrollmentApplicationService;
import training.cqrstraining.infrastructure.event.EnrollmentChangeSseService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class EnrollmentController {

    private final EnrollmentApplicationService enrollmentService;
    private final EnrollmentChangeSseService enrollmentChangeSseService;

    public EnrollmentController(EnrollmentApplicationService enrollmentService,
                                EnrollmentChangeSseService enrollmentChangeSseService) {
        this.enrollmentService = enrollmentService;
        this.enrollmentChangeSseService = enrollmentChangeSseService;
    }

    @PostMapping("/enrollments")
    public ResponseEntity<EnrollmentDto> enroll(@Valid @RequestBody CreateEnrollmentCommand command) {
        EnrollmentDto dto = enrollmentService.enroll(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.ETAG, toEtag(dto.version()))
                .body(dto);
    }

    @PostMapping("/courses/{courseId}/cancellations")
    public ResponseEntity<EnrollmentDto> cancel(
            @PathVariable @Positive Long courseId,
            @Valid @RequestBody CreateCancellationRequest request
    ) {
        EnrollmentDto dto = enrollmentService.cancel(new CreateCancellationCommand(courseId, request.employeeIds()));
        return ResponseEntity.ok()
                .header(HttpHeaders.ETAG, toEtag(dto.version()))
                .body(dto);
    }

    @GetMapping("/enrollments")
    public List<EnrollmentDto> listAll() {
        return enrollmentService.listAll();
    }

    @GetMapping("/courses/{courseId}/enrollments")
    public ResponseEntity<EnrollmentDto> listByCourse(@PathVariable @Positive Long courseId) {
        EnrollmentDto dto = enrollmentService.listByCourse(courseId);
        return ResponseEntity.ok()
                .header(HttpHeaders.ETAG, toEtag(dto.version()))
                .body(dto);
    }

    @GetMapping("/courses/enrollment-counts")
    public List<CourseEnrollmentCountDto> getEnrollmentCounts() {
        return enrollmentService.countEnrollmentsByCourse();
    }

    @GetMapping("/courses/enrollment-counts/{courseId}")
    public ResponseEntity<CourseEnrollmentCountDto> getEnrollmentCountByCourse(@PathVariable @Positive Long courseId) {
        CourseEnrollmentCountDto dto = enrollmentService.countEnrollmentsByCourse(courseId);
        return ResponseEntity.ok()
                .header(HttpHeaders.ETAG, toEtag(dto.version()))
                .body(dto);
    }

    @GetMapping(path = "/enrollments/changes", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEnrollmentChanges() {
        return enrollmentChangeSseService.subscribe();
    }

    private String toEtag(Long version) {
        long safeVersion = version == null ? 0L : version;
        return "\"" + safeVersion + "\"";
    }
}
