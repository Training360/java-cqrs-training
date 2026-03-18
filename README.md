# CQRS Training - Course Enrollments

Three-layer Spring Boot application with DDD-inspired boundaries for managing employee enrollments on courses.

## Stack

- Spring Boot (Web, Validation, Data JPA)
- H2 in-memory database
- Maven

## Layers

- `api`: REST controllers and exception mapping
- `application`: use-case orchestration (`EnrollmentApplicationService`)
- `domain`: core model, domain rules, repository ports
- `infrastructure`: JPA entities and repository adapter implementation

## Run

```cmd
mvnw.cmd spring-boot:run
```

## Endpoints

- `POST /api/enrollments`
  - body: `{ "courseId": 10, "employeeIds": [200, 201] }`
  - response: `{ "courseId": 10, "employeeIds": [200, 201] }`
- `POST /api/courses/{courseId}/deregistrations`
  - body: `{ "employeeIds": [200] }`
  - response: `{ "courseId": 10, "employeeIds": [201] }`
- `GET /api/enrollments`
- `GET /api/courses/{courseId}/enrollments`
  - response: `{ "courseId": 10, "employeeIds": [200, 201] }`

## H2 Console

- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:cqrs-training-db`
- user: `sa`
- password: *(empty)*
