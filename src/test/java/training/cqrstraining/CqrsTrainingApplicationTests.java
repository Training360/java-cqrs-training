package training.cqrstraining;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import training.cqrstraining.api.CreateCancellationRequest;
import training.cqrstraining.application.command.CreateEnrollmentCommand;
import training.cqrstraining.application.dto.CourseEnrollmentCountDto;
import training.cqrstraining.application.dto.EnrollmentDto;
import training.cqrstraining.infrastructure.persistence.OutboxEventJpaRepository;
import training.cqrstraining.infrastructure.persistence.OutboxEventStatus;

import java.util.Map;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class CqrsTrainingApplicationTests {

    @Autowired
    RestTestClient restTestClient;

    @Autowired
    OutboxEventJpaRepository outboxEventRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void enrollAndListByCourse() {
        var enrollResult = restTestClient.post().uri("/api/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateEnrollmentCommand(10L, List.of(200L, 201L)))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(EnrollmentDto.class)
                .returnResult();

        EnrollmentDto enrolled = enrollResult.getResponseBody();

        assertThat(enrolled).isNotNull();
        assertThat(enrolled.courseId()).isEqualTo(10L);
        assertThat(enrolled.employeeIds()).containsExactly(200L, 201L);
        assertThat(enrolled.version()).isNotNull();
        assertThat(enrollResult.getResponseHeaders().getETag()).isEqualTo("\"" + enrolled.version() + "\"");

        var listResult = restTestClient.get().uri("/api/courses/10/enrollments")
                .exchange()
                .expectStatus().isOk()
                .expectBody(EnrollmentDto.class)
                .returnResult();

        EnrollmentDto listed = listResult.getResponseBody();

        assertThat(listed).isNotNull();
        assertThat(listed.courseId()).isEqualTo(10L);
        assertThat(listed.employeeIds()).containsExactly(200L, 201L);
        assertThat(listed.version()).isEqualTo(enrolled.version());
        assertThat(listResult.getResponseHeaders().getETag()).isEqualTo("\"" + listed.version() + "\"");
    }

    @Test
    void duplicateEnrollmentReturnsConflict() {
        restTestClient.post().uri("/api/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateEnrollmentCommand(11L, List.of(201L)))
                .exchange()
                .expectStatus().isCreated();

        restTestClient.post().uri("/api/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateEnrollmentCommand(11L, List.of(201L, 202L)))
                .exchange()
                .expectStatus().isEqualTo(409);

        EnrollmentDto listed = restTestClient.get().uri("/api/courses/11/enrollments")
                .exchange()
                .expectStatus().isOk()
                .expectBody(EnrollmentDto.class)
                .returnResult().getResponseBody();

        assertThat(listed).isNotNull();
        assertThat(listed.employeeIds()).containsExactly(201L);
    }

    @Test
    void cancelEmployeesThroughOperationResource() {
        EnrollmentDto beforeCancellation = restTestClient.post().uri("/api/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateEnrollmentCommand(12L, List.of(300L, 301L, 302L)))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(EnrollmentDto.class)
                .returnResult().getResponseBody();

        assertThat(beforeCancellation).isNotNull();

        var cancellationResult = restTestClient.post().uri("/api/courses/12/cancellations")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateCancellationRequest(List.of(300L, 302L)))
                .exchange()
                .expectStatus().isOk()
                .expectBody(EnrollmentDto.class)
                .returnResult();

        EnrollmentDto afterCancellation = cancellationResult.getResponseBody();

        assertThat(afterCancellation).isNotNull();
        assertThat(afterCancellation.courseId()).isEqualTo(12L);
        assertThat(afterCancellation.employeeIds()).containsExactly(301L);
        assertThat(afterCancellation.version()).isGreaterThan(beforeCancellation.version());
        assertThat(cancellationResult.getResponseHeaders().getETag()).isEqualTo("\"" + afterCancellation.version() + "\"");

        EnrollmentDto listed = restTestClient.get().uri("/api/courses/12/enrollments")
                .exchange()
                .expectStatus().isOk()
                .expectBody(EnrollmentDto.class)
                .returnResult().getResponseBody();

        assertThat(listed).isNotNull();
        assertThat(listed.courseId()).isEqualTo(12L);
        assertThat(listed.employeeIds()).containsExactly(301L);
    }

    @Test
    void queryEnrollmentCountsByCourse() {
        // Enroll employees to different courses
        restTestClient.post().uri("/api/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateEnrollmentCommand(20L, List.of(400L, 401L, 402L)))
                .exchange()
                .expectStatus().isCreated();

        restTestClient.post().uri("/api/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateEnrollmentCommand(21L, List.of(410L, 411L)))
                .exchange()
                .expectStatus().isCreated();

        restTestClient.post().uri("/api/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateEnrollmentCommand(22L, List.of(420L)))
                .exchange()
                .expectStatus().isCreated();

        // Cancel one employee to verify decrement in the read-model projection
        restTestClient.post().uri("/api/courses/20/cancellations")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateCancellationRequest(List.of(401L)))
                .exchange()
                .expectStatus().isOk();

        // Query enrollment counts
        var counts = awaitEnrollmentCounts(() -> restTestClient.get().uri("/api/courses/enrollment-counts")
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<List<CourseEnrollmentCountDto>>() {})
                .returnResult().getResponseBody());

        assertThat(counts).isNotNull();
        Map<Long, Long> countsByCourse = counts.stream()
                .collect(java.util.stream.Collectors.toMap(
                        CourseEnrollmentCountDto::courseId,
                        CourseEnrollmentCountDto::enrollmentCount,
                        (existing, ignored) -> existing
                ));

        assertThat(countsByCourse).containsEntry(20L, 2L);
        assertThat(countsByCourse).containsEntry(21L, 2L);
        assertThat(countsByCourse).containsEntry(22L, 1L);
    }

    @Test
    void queryEnrollmentCountBySingleCourse() {
        restTestClient.post().uri("/api/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateEnrollmentCommand(23L, List.of(430L, 431L, 432L)))
                .exchange()
                .expectStatus().isCreated();

        restTestClient.post().uri("/api/courses/23/cancellations")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateCancellationRequest(List.of(431L)))
                .exchange()
                .expectStatus().isOk();

        CourseEnrollmentCountDto count = awaitEnrollmentCountByCourse(23L, 2L);

        assertThat(count.courseId()).isEqualTo(23L);
        assertThat(count.enrollmentCount()).isEqualTo(2L);
        assertThat(count.version()).isNotNull();
    }

    @Test
    void queryEnrollmentCountBySingleCourseReturnsZeroWhenMissing() {
        CourseEnrollmentCountDto count = restTestClient.get().uri("/api/courses/enrollment-counts/99999")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CourseEnrollmentCountDto.class)
                .returnResult().getResponseBody();

        assertThat(count).isNotNull();
        assertThat(count.courseId()).isEqualTo(99999L);
        assertThat(count.enrollmentCount()).isZero();
        assertThat(count.version()).isZero();
    }

    @Test
    void outboxEventsAreEventuallyPublished() {
        restTestClient.post().uri("/api/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateEnrollmentCommand(30L, List.of(501L, 502L)))
                .exchange()
                .expectStatus().isCreated();

        awaitPublishedOutboxAtLeast(1L);

        assertThat(outboxEventRepository.countByStatus(OutboxEventStatus.PUBLISHED)).isGreaterThanOrEqualTo(1L);
        assertThat(outboxEventRepository.countByStatus(OutboxEventStatus.DEAD)).isZero();
    }

    private List<CourseEnrollmentCountDto> awaitEnrollmentCounts(Supplier<List<CourseEnrollmentCountDto>> fetcher) {
        for (int attempt = 0; attempt < 20; attempt++) {
            List<CourseEnrollmentCountDto> counts = fetcher.get();
            if (counts != null) {
                Map<Long, Long> countsByCourse = counts.stream()
                        .collect(java.util.stream.Collectors.toMap(
                                CourseEnrollmentCountDto::courseId,
                                CourseEnrollmentCountDto::enrollmentCount,
                                (existing, ignored) -> existing
                        ));

                if (countsByCourse.getOrDefault(20L, 0L) == 2L
                        && countsByCourse.getOrDefault(21L, 0L) == 2L
                        && countsByCourse.getOrDefault(22L, 0L) == 1L) {
                    return counts;
                }
            }

            try {
                Thread.sleep(100L);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for enrollment counts projection.", ex);
            }
        }

        return fetcher.get();
    }

    private void awaitPublishedOutboxAtLeast(long minimumPublished) {
        for (int attempt = 0; attempt < 20; attempt++) {
            if (outboxEventRepository.countByStatus(OutboxEventStatus.PUBLISHED) >= minimumPublished) {
                return;
            }

            try {
                Thread.sleep(100L);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for outbox publication.", ex);
            }
        }
    }

    private CourseEnrollmentCountDto awaitEnrollmentCountByCourse(long courseId, long expectedCount) {
        for (int attempt = 0; attempt < 20; attempt++) {
            CourseEnrollmentCountDto count = restTestClient.get().uri("/api/courses/enrollment-counts/" + courseId)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(CourseEnrollmentCountDto.class)
                    .returnResult().getResponseBody();

            if (count != null && count.enrollmentCount() != null && count.enrollmentCount() == expectedCount) {
                return count;
            }

            try {
                Thread.sleep(100L);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for single-course enrollment count projection.", ex);
            }
        }

        return restTestClient.get().uri("/api/courses/enrollment-counts/" + courseId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CourseEnrollmentCountDto.class)
                .returnResult().getResponseBody();
    }
}
