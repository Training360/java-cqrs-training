package training.cqrstraining.application.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import training.cqrstraining.application.command.CreateEnrollmentCommand;
import training.cqrstraining.application.command.CreateDeregistrationCommand;
import training.cqrstraining.application.dto.CourseEnrollmentCountDto;
import training.cqrstraining.application.dto.EnrollmentDto;
import training.cqrstraining.application.query.EnrollmentQueryRepository;
import training.cqrstraining.domain.exception.EnrollmentNotFoundException;
import training.cqrstraining.domain.model.CourseId;
import training.cqrstraining.domain.model.EmployeeId;
import training.cqrstraining.domain.model.Enrollment;
import training.cqrstraining.domain.repository.EnrollmentRepository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EnrollmentApplicationService {

    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentQueryRepository enrollmentQueryRepository;
    private final ApplicationEventPublisher eventPublisher;

    public EnrollmentApplicationService(EnrollmentRepository enrollmentRepository,
                                        EnrollmentQueryRepository enrollmentQueryRepository,
                                        ApplicationEventPublisher eventPublisher) {
        this.enrollmentRepository = enrollmentRepository;
        this.enrollmentQueryRepository = enrollmentQueryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public EnrollmentDto enroll(CreateEnrollmentCommand command) {
        CourseId courseId = new CourseId(command.courseId());

        Enrollment enrollment = enrollmentRepository.findByCourseId(courseId)
                .orElseGet(() -> new Enrollment(courseId));

        Set<EmployeeId> employeeIds = command.employeeIds().stream()
                .map(EmployeeId::new)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        enrollment.enrollAll(employeeIds);
        // Itt kell elküldeni, mert a saved példányban már nem lesznek benne
        enrollment.pullEvents().forEach(eventPublisher::publishEvent);
        Enrollment saved = enrollmentRepository.save(enrollment);

        return toDto(saved);
    }

    @Transactional
    public EnrollmentDto deregister(CreateDeregistrationCommand command) {
        CourseId courseId = new CourseId(command.courseId());

        Enrollment enrollment = enrollmentRepository.findByCourseId(courseId)
                .orElseThrow(() -> new EnrollmentNotFoundException(courseId.value(), command.employeeIds().getFirst()));

        Set<EmployeeId> employeeIds = command.employeeIds().stream()
                .map(EmployeeId::new)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        enrollment.cancelAll(employeeIds);
        enrollment.pullEvents().forEach(eventPublisher::publishEvent);

        Enrollment saved = enrollmentRepository.save(enrollment);

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public EnrollmentDto listByCourse(Long courseIdValue) {
        CourseId courseId = new CourseId(courseIdValue);
        return enrollmentRepository.findByCourseId(courseId)
                .map(this::toDto)
                .orElse(new EnrollmentDto(courseId.value(), List.of()));
    }

    @Transactional(readOnly = true)
    public List<EnrollmentDto> listAll() {
        return enrollmentRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CourseEnrollmentCountDto> countEnrollmentsByCourse() {
        return enrollmentQueryRepository.countEnrollmentsByCourse();
    }

    private EnrollmentDto toDto(Enrollment enrollment) {
        List<Long> employeeIds = enrollment.employeeIds()
                .stream()
                .map(EmployeeId::value)
                .sorted()
                .toList();
        return new EnrollmentDto(enrollment.courseId().value(), employeeIds);
    }
}
