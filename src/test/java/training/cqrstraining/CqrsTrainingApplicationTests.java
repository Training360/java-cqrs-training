package training.cqrstraining;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import training.cqrstraining.api.CreateDeregistrationRequest;
import training.cqrstraining.api.EnrollmentController;
import training.cqrstraining.application.command.CreateEnrollmentCommand;
import training.cqrstraining.application.dto.EnrollmentDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class CqrsTrainingApplicationTests {

    @Autowired
    EnrollmentController enrollmentController;

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
}
