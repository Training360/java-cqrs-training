package training.cqrstraining;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CqrsTrainingApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void contextLoads() {
    }

    @Test
    void enrollAndListByCourse() throws Exception {
        String createPayload = objectMapper.writeValueAsString(new EnrollmentRequest(10L, List.of(200L, 201L)));

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseId").value(10))
                .andExpect(jsonPath("$.employeeIds[0]").value(200))
                .andExpect(jsonPath("$.employeeIds[1]").value(201));

        mockMvc.perform(get("/api/courses/10/enrollments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(10))
                .andExpect(jsonPath("$.employeeIds[0]").value(200))
                .andExpect(jsonPath("$.employeeIds[1]").value(201));
    }

    @Test
    void duplicateEnrollmentReturnsConflict() throws Exception {
        String createPayload = objectMapper.writeValueAsString(new EnrollmentRequest(11L, List.of(201L)));
        String conflictingPayload = objectMapper.writeValueAsString(new EnrollmentRequest(11L, List.of(201L, 202L)));

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(conflictingPayload))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/courses/11/enrollments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeIds.length()").value(1))
                .andExpect(jsonPath("$.employeeIds[0]").value(201));
    }

    @Test
    void deregisterEmployeesThroughOperationResource() throws Exception {
        String createPayload = objectMapper.writeValueAsString(new EnrollmentRequest(12L, List.of(300L, 301L, 302L)));
        String deregistrationPayload = objectMapper.writeValueAsString(new DeregistrationRequest(List.of(300L, 302L)));

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/courses/12/deregistrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(deregistrationPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(12))
                .andExpect(jsonPath("$.employeeIds.length()").value(1))
                .andExpect(jsonPath("$.employeeIds[0]").value(301));

        mockMvc.perform(get("/api/courses/12/enrollments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(12))
                .andExpect(jsonPath("$.employeeIds.length()").value(1))
                .andExpect(jsonPath("$.employeeIds[0]").value(301));
    }

    private record EnrollmentRequest(Long courseId, List<Long> employeeIds) {
    }

    private record DeregistrationRequest(List<Long> employeeIds) {
    }

}
