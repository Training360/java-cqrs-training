package training.cqrstraining;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import training.cqrstraining.api.CreateDeregistrationRequest;
import training.cqrstraining.application.command.CreateEnrollmentCommand;
import training.cqrstraining.application.dto.CourseEnrollmentCountDto;
import training.cqrstraining.application.dto.EnrollmentDto;

import java.util.Map;
import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class CqrsTrainingApplicationTests {

    @Autowired
    RestTestClient restTestClient;

    @Test
    void contextLoads() {
    }

    @Test
    void enrollAndListByCourse() {
        EnrollmentDto enrolled = restTestClient.post().uri("/api/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateEnrollmentCommand(10L, List.of(200L, 201L)))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(EnrollmentDto.class)
                .returnResult().getResponseBody();

        assertThat(enrolled).isNotNull();
        assertThat(enrolled.courseId()).isEqualTo(10L);
        assertThat(enrolled.employeeIds()).containsExactly(200L, 201L);

        EnrollmentDto listed = restTestClient.get().uri("/api/courses/10/enrollments")
                .exchange()
                .expectStatus().isOk()
                .expectBody(EnrollmentDto.class)
                .returnResult().getResponseBody();

        assertThat(listed).isNotNull();
        assertThat(listed.courseId()).isEqualTo(10L);
        assertThat(listed.employeeIds()).containsExactly(200L, 201L);
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
    void deregisterEmployeesThroughOperationResource() {
        restTestClient.post().uri("/api/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateEnrollmentCommand(12L, List.of(300L, 301L, 302L)))
                .exchange()
                .expectStatus().isCreated();

        EnrollmentDto afterDeregister = restTestClient.post().uri("/api/courses/12/deregistrations")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateDeregistrationRequest(List.of(300L, 302L)))
                .exchange()
                .expectStatus().isOk()
                .expectBody(EnrollmentDto.class)
                .returnResult().getResponseBody();

        assertThat(afterDeregister).isNotNull();
        assertThat(afterDeregister.courseId()).isEqualTo(12L);
        assertThat(afterDeregister.employeeIds()).containsExactly(301L);

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

        // Deregister one employee to verify decrement in the read-model projection
        restTestClient.post().uri("/api/courses/20/deregistrations")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateDeregistrationRequest(List.of(401L)))
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
}
