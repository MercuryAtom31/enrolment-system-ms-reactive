package com.champlain.courseservice.presentationlayer;

import com.champlain.courseservice.businesslayer.CourseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * This class is a unit test for the CourseController.
 * It uses WebTestClient to simulate HTTP requests
 * and test the endpoints in your controller.
 */

/**
 * This annotation is used to test a specific controller (CourseController in this case)
 * and disables the full Spring Boot application context
 * to focus only on web-related components.
 */
@WebFluxTest(controllers = CourseController.class)
public class CourseControllerUnitTest {

    @Autowired
    private CourseController courseController;

    @Autowired
    private WebTestClient webTestClient;
    /**
     * @MockBean: This tells Spring to create a mock (simulate) of the CourseService bean.
     * The mock is injected into the controller, and you can use it to control
     * the behavior of the service layer in your tests.
     */
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
        /**
         * You generate a courseId and create a CourseResponseModel,
         * which represents the expected response from the API.
         */
        String courseId = UUID.randomUUID().toString();

        CourseResponseModel courseResponseModel = CourseResponseModel.builder()
                .courseId(courseId)
                .courseNumber(courseRequestModel.getCourseNumber())
                .courseName(courseRequestModel.getCourseName())
                .numHours(courseRequestModel.getNumHours())
                .numCredits(courseRequestModel.getNumCredits())
                .department(courseRequestModel.getDepartment())
                .build();
        /**
         * When you call the .addCourse() method from the CourseService,
         * return a CourseResponseModel.
         */
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
        /**
         * The times(1) is used to specify how many times you expect the addCourse method
         * of courseService to be called during the test.
         */
        /**
         * The .addCourse(any(Mono.class)) is used to verify that the addCourse() method
         * of the courseService was called with an argument of type Mono<CourseRequestModel>.
         *
         * Why any(Mono.class)?
         * This is a way to generalize the verification process.
         * Instead of checking the exact content of the Mono,
         * you're just checking that some Mono was passed to the addCourse() method.
         */
        verify(courseService, times(1)).addCourse(any(Mono.class));
    }

    @Test
    void getCourseByCourseId_validCourseId_courseReturned(){

        // Arrange
        String validCourseId = UUID.randomUUID().toString();
        CourseResponseModel courseResponseModel = CourseResponseModel.builder()
                .courseId(validCourseId)
                .courseNumber("cat-420")
                .courseName("Web Services")
                .numHours(45)
                .numCredits(3.0)
                .department("Computer Science")
                .build();

        // Mocking (Simulating) the service call to return the expected course when the valid courseId is passed
        when(courseService.getCourseByCourseId(validCourseId))
                .thenReturn(Mono.just(courseResponseModel));

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/courses/" + validCourseId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .expectBody(CourseResponseModel.class)
                .isEqualTo(courseResponseModel);

        // Verifying that the service method was called once
        verify(courseService, times(1)).getCourseByCourseId(validCourseId);

    }
    @Test
    public void whenGetCourseByCourseId_withInvalidCourseId_thenReturnInvalidInputException() {
        // Arrange
        String invalidCourseId = "12345"; // Invalid courseId with fewer than 36 characters

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/courses/" + invalidCourseId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()  // Execute the request
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided Course id is invalid: " + invalidCourseId);
    }

    @Test
    public void whenUpdateCourse_thenReturnCourseResponseModel(){
        // Arrange
        String courseId = UUID.randomUUID().toString();

        CourseRequestModel updatedCourseRequestModel = CourseRequestModel.builder()
                .courseNumber("cat-999")
                .courseName("Updated Web Services")
                .numHours(50)
                .numCredits(4.0)
                .department("Updated Department")
                .build();

        CourseResponseModel updatedCourseResponseModel = CourseResponseModel.builder()
                .courseId(courseId)
                .courseNumber(updatedCourseRequestModel.getCourseNumber())
                .courseName(updatedCourseRequestModel.getCourseName())
                .numHours(updatedCourseRequestModel.getNumHours())
                .numCredits(updatedCourseRequestModel.getNumCredits())
                .department(updatedCourseRequestModel.getDepartment())
                .build();

        when(courseService.updateCourseByCourseId(any(Mono.class), eq(courseId)))
                .thenReturn(Mono.just(updatedCourseResponseModel));

        // Act & Assert
        webTestClient.put()
                .uri("/api/v1/courses/" + courseId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updatedCourseRequestModel), CourseRequestModel.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .expectBody(CourseResponseModel.class)
                .isEqualTo(updatedCourseResponseModel);

        // Verifying that the service method was called once with the correct parameters
        verify(courseService, times(1))
                .updateCourseByCourseId(any(Mono.class), eq(courseId));

    }
    @Test
    public void whenUpdateCourse_withInvalidCourseId_thenReturnInvalidInputException(){
        // Arrange
        String invalidCourseId = "123456789";

        CourseRequestModel updatedCourseRequestModel = CourseRequestModel.builder()
                .courseNumber("cat-999")
                .courseName("Updated Web Services")
                .numHours(50)
                .numCredits(4.0)
                .department("Updated Department")
                .build();

        // Act & Assert
        webTestClient.put()
                .uri("/api/v1/courses/" + invalidCourseId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(updatedCourseRequestModel), CourseRequestModel.class)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("Provided Course Id is invalid: " + invalidCourseId);
    }
    @Test
    public void whenDeleteCourse_thenReturnCourseResponseModel(){
        //Arrange
        String courseId = UUID.randomUUID().toString();

        CourseResponseModel courseResponseModel = CourseResponseModel.builder()
                .courseId(courseId)
                .courseNumber("cat-420")
                .courseName("Web Services")
                .numHours(45)
                .numCredits(3.0)
                .department("Computer Science")
                .build();

        when(courseService.deleteCourseByCourseId(courseId))
                .thenReturn(Mono.just(courseResponseModel));

        // Act & Assert
        webTestClient.delete()
                .uri("/api/v1/courses/" + courseId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .expectBody(CourseResponseModel.class)
                .isEqualTo(courseResponseModel);

        verify(courseService, times(1)).deleteCourseByCourseId(courseId);
    }
    @Test
    public void whenDeleteCourse_withInvalidCourseId_thenReturnInvalidInputException(){
        // Arrange
        String invalidCourseId = "12345";

        // Act & Assert
        webTestClient.delete()
                .uri("/api/v1/courses/" + invalidCourseId)
                .exchange()  // Execute the request
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided Course id is invalid: " + invalidCourseId);
    }
}
