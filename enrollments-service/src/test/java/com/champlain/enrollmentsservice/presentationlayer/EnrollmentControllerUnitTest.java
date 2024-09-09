package com.champlain.enrollmentsservice.presentationlayer;

import com.champlain.enrollmentsservice.businesslayer.enrollments.EnrollmentService;
import com.champlain.enrollmentsservice.dataaccesslayer.Semester;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentController;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentRequestModel;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentResponseModel;
import com.champlain.enrollmentsservice.utils.exceptions.InvalidInputException;
import com.champlain.enrollmentsservice.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
@WebFluxTest(controllers = EnrollmentController.class)

public class EnrollmentControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;
    @MockBean
    private EnrollmentService enrollmentService;

    String enrollmentId = UUID.randomUUID().toString();
    EnrollmentResponseModel enrollmentResponseModel = EnrollmentResponseModel.builder()
            .enrollmentId(enrollmentId)
            .enrollmentYear(2023)
            .semester(Semester.FALL)
            .studentId("student123")
            .studentFirstName("Donna")
            .studentLastName("Hornsby")
            .courseId("course123")
            .courseName("Web Services")
            .courseNumber("N45-LA")
            .build();
    EnrollmentRequestModel enrollmentRequestModel = EnrollmentRequestModel.builder()
            .enrollmentYear(2023)
            .semester(Semester.FALL)
            .studentId("student123")
            .courseId("course123")
            .build();

    @Test
    void getEnrollmentByEnrollmentId_validEnrollmentId_enrollmentReturned() {
        // Arrange
        when(enrollmentService.getEnrollmentByEnrollmentId(enrollmentId))
                .thenReturn(Mono.just(enrollmentResponseModel));
        // Act
        webTestClient.get()
                .uri("/api/v1/enrollments/{enrollmentId}", enrollmentId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(EnrollmentResponseModel.class)
                .isEqualTo(enrollmentResponseModel);
        verify(enrollmentService, times(1))
                .getEnrollmentByEnrollmentId(enrollmentId);
    }

    @Test
    void addEnrollment_validEnrollment_enrollmentAdded(){
        // Arrange
        when(enrollmentService.addEnrollment(any(Mono.class)))
                .thenReturn(Mono.just(enrollmentResponseModel));

        // Act
        webTestClient.post()
                .uri("/api/v1/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                // Passing the enrollment request model as body
                .body(Mono.just(enrollmentRequestModel), EnrollmentRequestModel.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(EnrollmentResponseModel.class)
                .isEqualTo(enrollmentResponseModel);  // Verifying the response matches the expected model

        // Assert
        verify(enrollmentService, times(1)).addEnrollment(any(Mono.class));
    }
    @Test
    public void getEnrollmentByEnrollmentId_withInvalidEnrollmentId_throwsNotFoundException() {
        // Arrange
        String invalidEnrollmentId = UUID.randomUUID().toString();

        when(enrollmentService.getEnrollmentByEnrollmentId(invalidEnrollmentId))
                .thenReturn(Mono.error(new NotFoundException("Enrollment id not found: " + invalidEnrollmentId)));

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/enrollments/{enrollmentId}", invalidEnrollmentId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .consumeWith(response -> System.out.println(new String(response.getResponseBody())))
                .jsonPath("$.message").isEqualTo("Enrollment id not found: " + invalidEnrollmentId);
    }

    @Test
    void getAllEnrollments_validEnrollments_enrollmentsReturned() {
        // Arrange
        EnrollmentResponseModel enrollment1 = EnrollmentResponseModel.builder()
                .enrollmentId(UUID.randomUUID().toString())
                .enrollmentYear(2023)
                .semester(Semester.FALL)
                .studentId("student123")
                .studentFirstName("Donna")
                .studentLastName("Hornsby")
                .courseId("course123")
                .courseName("Web Services")
                .courseNumber("N45-LA")
                .build();

        EnrollmentResponseModel enrollment2 = EnrollmentResponseModel.builder()
                .enrollmentId(UUID.randomUUID().toString())
                .enrollmentYear(2023)
                .semester(Semester.FALL)
                .studentId("student456")
                .studentFirstName("John")
                .studentLastName("Doe")
                .courseId("course456")
                .courseName("Data Science")
                .courseNumber("DS-101")
                .build();

        when(enrollmentService.getAllEnrollments()).thenReturn(Flux.just(enrollment1, enrollment2));

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/enrollments")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(EnrollmentResponseModel.class)
                .hasSize(2)  // Expecting two enrollments
                .value(enrollments -> {
                    assertEquals("student123", enrollments.get(0).getStudentId());
                    assertEquals("Web Services", enrollments.get(0).getCourseName());
                    assertEquals("student456", enrollments.get(1).getStudentId());
                    assertEquals("Data Science", enrollments.get(1).getCourseName());
                });

        verify(enrollmentService, times(1)).getAllEnrollments();
    }


    @Test
    public void updateEnrollment_validEnrollment_enrollmentUpdated() {
        // Arrange
        String enrollmentId = UUID.randomUUID().toString();

        EnrollmentRequestModel updatedRequestModel = EnrollmentRequestModel.builder()
                .enrollmentYear(2024)
                .semester(Semester.SPRING)
                .studentId("updatedStudent123")
                .courseId("updatedCourse123")
                .build();

        EnrollmentResponseModel updatedResponseModel = EnrollmentResponseModel.builder()
                .enrollmentId(enrollmentId)
                .enrollmentYear(2024)
                .semester(Semester.SPRING)
                .studentId("updatedStudent123")
                .studentFirstName("UpdatedFirstName")
                .studentLastName("UpdatedLastName")
                .courseId("updatedCourse123")
                .courseNumber("UpdatedCourseNumber")
                .courseName("UpdatedCourseName")
                .build();

        when(enrollmentService.updateEnrollmentByEnrollmentId(any(Mono.class), eq(enrollmentId)))
                .thenReturn(Mono.just(updatedResponseModel));

        // Act
        webTestClient.put()
                .uri("/api/v1/enrollments/{enrollmentId}", enrollmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updatedRequestModel), EnrollmentRequestModel.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(EnrollmentResponseModel.class)
                .isEqualTo(updatedResponseModel);

        // Assert
        verify(enrollmentService, times(1))
                .updateEnrollmentByEnrollmentId(any(Mono.class), eq(enrollmentId));
    }

    @Test
    public void updateEnrollment_withInvalidEnrollmentId_throwsInvalidInputException() {
        // Arrange
        String invalidEnrollmentId = "invalid-enrollment-id";

        EnrollmentRequestModel enrollmentRequestModel = EnrollmentRequestModel.builder()
                .enrollmentYear(2024)
                .semester(Semester.SPRING)
                .studentId("student123")
                .courseId("course123")
                .build();

        when(enrollmentService.updateEnrollmentByEnrollmentId(any(Mono.class), eq(invalidEnrollmentId)))
                .thenThrow(new InvalidInputException("Provided Enrollment ID is invalid: " + invalidEnrollmentId));

        // Act
        webTestClient.put()
                .uri("/api/v1/enrollments/{enrollmentId}", invalidEnrollmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(enrollmentRequestModel), EnrollmentRequestModel.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided Enrollment ID is invalid: " + invalidEnrollmentId);

        // Assert
        verify(enrollmentService, times(1)).updateEnrollmentByEnrollmentId(any(Mono.class), eq(invalidEnrollmentId));
    }

    @Test
    public void deleteEnrollment_validEnrollmentId_enrollmentDeleted() {
        // Arrange
        String enrollmentId = UUID.randomUUID().toString();

        EnrollmentResponseModel enrollmentResponseModel = EnrollmentResponseModel.builder()
                .enrollmentId(enrollmentId)
                .enrollmentYear(2023)
                .semester(Semester.FALL)
                .studentId("student123")
                .studentFirstName("Donna")
                .studentLastName("Hornsby")
                .courseId("course123")
                .courseNumber("N45-LA")
                .courseName("Web Services")
                .build();

        when(enrollmentService.deleteEnrollmentByEnrollmentId(enrollmentId))
                .thenReturn(Mono.just(enrollmentResponseModel));

        // Act
        webTestClient.delete()
                .uri("/api/v1/enrollments/{enrollmentId}", enrollmentId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(EnrollmentResponseModel.class)
                .isEqualTo(enrollmentResponseModel);

        // Assert
        verify(enrollmentService, times(1)).deleteEnrollmentByEnrollmentId(enrollmentId);
    }

    @Test
    public void deleteEnrollment_withInvalidEnrollmentId_throwsInvalidInputException() {
        // Arrange
        String invalidEnrollmentId = "InvalidEnrollment123";
        String errorMessage = "Provided Enrollment ID is invalid: " + invalidEnrollmentId;

        // Act
        webTestClient.delete()
                .uri("/api/v1/enrollments/{enrollmentId}", invalidEnrollmentId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message").isEqualTo(errorMessage);


        verify(enrollmentService, times(0))
                .deleteEnrollmentByEnrollmentId(invalidEnrollmentId);
    }
}
