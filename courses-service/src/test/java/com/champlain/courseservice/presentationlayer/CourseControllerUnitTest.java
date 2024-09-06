package com.champlain.courseservice.presentationlayer;

import com.champlain.courseservice.businesslayer.CourseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = CourseController.class)
public class CourseControllerUnitTest {

    @Autowired
    private CourseController courseController;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CourseService courseService;

    @Test
    public void whenAddCourse_thenReturnCourseResponseModel() {
        // Arrange
        CourseRequestModel courseRequestModel = CourseRequestModel.builder()
                .courseNumber("cat-420")
                .courseName("Web Services")
                .numHours(45)
                .numCredits(3.0)
                .department("Computer Science")
                .build();

        String courseId = UUID.randomUUID().toString();

        CourseResponseModel courseResponseModel = CourseResponseModel.builder()
                .courseId(courseId)
                .courseNumber(courseRequestModel.getCourseNumber())
                .courseName(courseRequestModel.getCourseName())
                .numHours(courseRequestModel.getNumHours())
                .numCredits(courseRequestModel.getNumCredits())
                .department(courseRequestModel.getDepartment())
                .build();
        when(courseService.addCourse(any(Mono.class)))
                .thenReturn(Mono.just(courseResponseModel));
        // Act
        webTestClient.post()
                .uri("/api/v1/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(courseRequestModel), CourseRequestModel.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(CourseResponseModel.class)
                .isEqualTo(courseResponseModel);

        verify(courseService, times(1)).addCourse(any(Mono.class));
    }

    @Test void getCourseByCourseId_validCourseId_courseReturned(){

    }
    @Test public void whenGetCourseByCourseId_withInvalidCourseId_thenReturnInvalidInputException(){

    }
    @Test public void whenUpdateCourse_thenReturnCourseResponseModel(){

    }
    @Test public void whenUpdateCourse_withInvalidCourseId_thenReturnInvalidInputException(){

    }
    @Test public void whenDeleteCourse_thenReturnCourseResponseModel(){

    }
    @Test public void whenDeleteCourse_withInvalidCourseId_thenReturnInvalidInputException(){

    }
}
