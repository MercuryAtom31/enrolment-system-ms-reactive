package com.champlain.enrollmentsservice.businesslayer;

import com.champlain.enrollmentsservice.businesslayer.enrollments.EnrollmentServiceImpl;
import com.champlain.enrollmentsservice.dataaccesslayer.Enrollment;
import com.champlain.enrollmentsservice.dataaccesslayer.EnrollmentRepository;
import com.champlain.enrollmentsservice.dataaccesslayer.Semester;
import com.champlain.enrollmentsservice.domainclientlayer.Courses.CourseClient;
import com.champlain.enrollmentsservice.domainclientlayer.Students.StudentClientAsynchronous;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentResponseModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static reactor.core.publisher.Mono.when;

@ExtendWith(MockitoExtension.class)
public class EnrollmentServiceUnitTest {

    @InjectMocks
    private EnrollmentServiceImpl enrollmentService;
    @Mock
    private EnrollmentRepository enrollmentRepository;
    @Mock
    private StudentClientAsynchronous studentClient;
    @Mock
    private CourseClient courseClient;

    Enrollment enrollment1 = Enrollment.builder()
            .id(UUID.randomUUID().toString())
            .enrollmentId(UUID.randomUUID().toString())
            .enrollmentYear(2023)
            .semester(Semester.FALL)
            .studentId("student123")
            .courseId("course123")
            .build();
    Enrollment enrollment2 = Enrollment.builder()
            .id(UUID.randomUUID().toString())
            .enrollmentId(UUID.randomUUID().toString())
            .enrollmentYear(2023)
            .semester(Semester.FALL)
            .studentId("student234")
            .courseId("course123")
            .build();

    @Test
    public void whenGetEnrollmentById_thenReturnEnrollment() {
        //arrange
        when(enrollmentRepository.findEnrollmentByEnrollmentId(enrollment1.getEnrollmentId()))
                .thenReturn(Mono.just(enrollment1));
        //act
        Mono<EnrollmentResponseModel> enrollment = enrollmentService.getEnrollmentByEnrollmentId(enrollment1.getEnrollmentId());
        //assert
        StepVerifier.create(enrollment)
                .expectNextMatches(enrollmentResponseModel -> {
                    assertNotNull(enrollmentResponseModel.getEnrollmentId());
                    assertEquals(enrollmentResponseModel.getEnrollmentYear(),
                            enrollment1.getEnrollmentYear());
                    assertEquals(enrollmentResponseModel.getSemester(),
                            enrollment1.getSemester());
                    assertEquals(enrollmentResponseModel.getStudentId(),
                            enrollment1.getStudentId());
                    assertEquals(enrollmentResponseModel.getStudentFirstName(),
                            enrollment1.getStudentFirstName());
                    assertEquals(enrollmentResponseModel.getStudentLastName(),
                            enrollment1.getStudentLastName());
                    assertEquals(enrollmentResponseModel.getCourseId(),
                            enrollment1.getCourseId());
                    assertEquals(enrollmentResponseModel.getCourseNumber(),
                            enrollment1.getCourseNumber());
                    assertEquals(enrollmentResponseModel.getCourseName(),
                            enrollment1.getCourseName());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    public void whenDeleteEnrollmentById_thenDeleteEnrollmentAndReturnEnrollmentResponseModel(){

    }
}

