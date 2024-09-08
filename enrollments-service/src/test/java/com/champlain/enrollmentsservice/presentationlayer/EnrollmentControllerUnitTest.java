package com.champlain.enrollmentsservice.presentationlayer;

import com.champlain.enrollmentsservice.businesslayer.enrollments.EnrollmentService;
import com.champlain.enrollmentsservice.dataaccesslayer.Semester;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentController;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentRequestModel;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentResponseModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.Mockito.*;
//import static reactor.core.publisher.Mono.when;

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

    }
    @Test
    public void getEnrollmentByEnrollmentId_withInvalidEnrollmentId_throwsNotFoundException(){

    }
    @Test
    public void getAllEnrollments_validEnrollments_enrollmentsReturned(){

    }
    @Test
    public void updateEnrollment_validEnrollment_enrollmentUpdated(){

    }
    @Test
    public void updateEnrollment_withInvalidEnrollmentId_throwsInvalidInputException(){

    }
    @Test
    public void deleteEnrollment_validEnrollmentId_enrollmentDeleted(){

    }
    @Test
    public void deleteEnrollment_withInvalidEnrollmentId_throwsInvalidInputException(){

    }
}
