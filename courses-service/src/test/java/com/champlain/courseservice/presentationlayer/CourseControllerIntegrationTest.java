package com.champlain.courseservice.presentationlayer;

import com.champlain.courseservice.dataaccesslayer.Course;
import com.champlain.courseservice.dataaccesslayer.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * This class is an integration test for your CourseController.
 * It tests the interaction between your controller and other parts of the application
 * (such as the database) using WebTestClient.
 * The class tests if the web endpoints behave as expected when dealing with
 * the real Spring Boot context and database setup.
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureWebTestClient
public class CourseControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CourseRepository courseRepository;

    private String validCourseId;
    /**
     * This represents the expected size of the database during tests
     * (e.g., 1000 courses are present in the test database).
     */
    private final Long dbSize = 1000L;

    @BeforeEach
    public void dbSetup() {
        // We just want to test that the db is at the original status every test.
        // Can be removed later
        StepVerifier.create(courseRepository.count())
                .consumeNextWith(count -> {
                    assertEquals(dbSize, count);
                })
                .verifyComplete();
    }

    @Test
    void addNewCourse_shouldSucceed() {
        CourseRequestModel courseRequestModel = CourseRequestModel.builder()
                .courseNumber("cat-423")
                .courseName("Web Services Testing")
                .numHours(45)
                .numCredits(3.0)
                .department("Computer Science")
                .build();

        webTestClient.post()
                .uri("/api/v1/courses")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(courseRequestModel)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(CourseResponseModel.class)
                .value(courseResponseModel -> {
                    assertNotNull(courseResponseModel);
                    assertNotNull(courseResponseModel.getCourseId());
                    assertEquals(courseRequestModel.getCourseNumber(),
                            courseResponseModel.getCourseNumber());
                    assertEquals(courseRequestModel.getCourseName(),
                            courseResponseModel.getCourseName());
                    assertEquals(courseRequestModel.getNumHours(),
                            courseResponseModel.getNumHours());
                    assertEquals(courseRequestModel.getNumCredits(),
                            courseResponseModel.getNumCredits());
                    assertEquals(courseRequestModel.getDepartment(),
                            courseResponseModel.getDepartment());
                });
    }

    @Test void getAllCourses_shouldReturnAllCourses() {

        webTestClient.get()
                .uri("/api/v1/courses")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .valueEquals("Content-Type", "text/event-stream;charset=UTF-8")
                .expectBodyList(CourseResponseModel.class)
                .value((list) -> {
                    assertNotNull(list);
                    assertEquals(dbSize, list.size());
                });
    }

    @Test
    void getCourseByCourseId_shouldSucceedWithExistingId(){
        // Arrange
        String existingCourseId = UUID.randomUUID().toString();

        Course course = Course.builder()
                .courseId(existingCourseId)
                .courseNumber("cat-420")
                .courseName("Web Services")
                .numHours(45)
                .numCredits(3.0)
                .department("Computer Science")
                .build();

        // Insert the course into the database
        StepVerifier
                .create(courseRepository.save(course))
                .expectNextCount(1)  // Expect the course to be saved
                .verifyComplete();

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/courses/" + existingCourseId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
                .expectBody(CourseResponseModel.class)
                .value(courseResponseModel -> {
                    assertNotNull(courseResponseModel);
                    assertEquals(existingCourseId, courseResponseModel.getCourseId());
                    assertEquals("cat-420", courseResponseModel.getCourseNumber());
                    assertEquals("Web Services", courseResponseModel.getCourseName());
                    assertEquals(45, courseResponseModel.getNumHours());
                    assertEquals(3.0, courseResponseModel.getNumCredits());
                    assertEquals("Computer Science", courseResponseModel.getDepartment());
                });
    }

    @Test
    void getCourseByCourseId_shouldReturnNotFound_WithNonExistingId(){
        // Arrange
        String nonExistingCourseId = UUID.randomUUID().toString();

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/courses/" + nonExistingCourseId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound();
    }
    @Test
    void getCourseByCourseId_shouldReturnUnProcessableEntity_WithInvalidId(){
        // Arrange
        String invalidCourseId = "123456789";  // Invalid courseId (less than 36 characters)

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/courses/" + invalidCourseId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()  // Execute the request
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided Course id is invalid: " + invalidCourseId);
    }

    /**
     * This test verifies that when there are no courses in the database,
     * the GET request to retrieve all courses returns an empty stream.
     */
    @Test
    void getAllCourses_whenNoCourses_shouldReturnEmptyStream(){
        // Arrange
        StepVerifier
                .create(courseRepository.deleteAll())  // Ensuring no courses are stored in the database
                .verifyComplete();

        // Act & Assert
        webTestClient.get()
                .uri("/api/v1/courses")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
                .expectBodyList(CourseResponseModel.class)
                .hasSize(0);  // Verifying that the list is empty
    }
    @Test
    void updateCourse_withValidCourseId_ShouldSucceed(){
        // Arrange
        String existingCourseId = UUID.randomUUID().toString();

        Course existingCourse = Course.builder()
                .courseId(existingCourseId)
                .courseNumber("cat-420")
                .courseName("Web Services")
                .numHours(45)
                .numCredits(3.0)
                .department("Computer Science")
                .build();

        // Inserting the existing course into the database
        StepVerifier.create(courseRepository.save(existingCourse))
                .expectNextCount(1)
                .verifyComplete();

        CourseRequestModel updatedCourseRequestModel = CourseRequestModel.builder()
                .courseNumber("cat-999")
                .courseName("Updated Web Services")
                .numHours(50)
                .numCredits(4.0)
                .department("Updated Department")
                .build();

        // Act & Assert
        webTestClient.put()
                .uri("/api/v1/courses/" + existingCourseId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedCourseRequestModel)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CourseResponseModel.class)
                .value(courseResponseModel -> {
                    assertNotNull(courseResponseModel);
                    assertEquals(existingCourseId, courseResponseModel.getCourseId());
                    assertEquals("cat-999", courseResponseModel.getCourseNumber());
                    assertEquals("Updated Web Services", courseResponseModel.getCourseName());
                    assertEquals(50, courseResponseModel.getNumHours());
                    assertEquals(4.0, courseResponseModel.getNumCredits());
                    assertEquals("Updated Department", courseResponseModel.getDepartment());
                });
    }
    @Test
    void updateCourse_withNonExistingCourseId_ShouldReturnNotFound(){
        // Arrange
        String nonExistingCourseId = UUID.randomUUID().toString();

        CourseRequestModel updatedCourseRequestModel = CourseRequestModel.builder()
                .courseNumber("cat-999")
                .courseName("Updated Web Services")
                .numHours(50)
                .numCredits(4.0)
                .department("Updated Department")
                .build();

        // Act & Assert
        webTestClient.put()
                .uri("/api/v1/courses/" + nonExistingCourseId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatedCourseRequestModel)
                .exchange()
                .expectStatus().isNotFound();
    }
    @Test
    void updateCourse_withInvalidCourseId_ShouldReturnUnProcessableEntity(){
        // Arrange
        String invalidCourseId = "123456789";  // Invalid courseId (less than 36 characters)

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
                .bodyValue(updatedCourseRequestModel)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Provided Course Id is invalid: " + invalidCourseId);
    }
    @Test
    void deleteCourse_withValidCourseId_ShouldReturnDeletedCourse(){
        /*
        1. Create valid id.
        2. Build a Course object, using the courseId.
        3. call the service method deleteCourse... Using WebTestClient.
        4. assert response status code 200 OK with response model.
         */
        // Arrange
        String existingCourseId = UUID.randomUUID().toString();

        Course course = Course.builder()
                .courseId(existingCourseId)
                .courseNumber("cat-420")
                .courseName("Web Services")
                .numHours(45)
                .numCredits(3.0)
                .department("Computer Science")
                .build();

        StepVerifier.create(courseRepository.save(course))
                .expectNextCount(1)
                .verifyComplete();

        // Act & Assert
        webTestClient.delete()
                .uri("/api/v1/courses/" + existingCourseId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CourseResponseModel.class)
                .value(courseResponseModel -> {
                    assertNotNull(courseResponseModel);
                    assertEquals(existingCourseId, courseResponseModel.getCourseId());
                    assertEquals("cat-420", courseResponseModel.getCourseNumber());
                    assertEquals("Web Services", courseResponseModel.getCourseName());
                    assertEquals(45, courseResponseModel.getNumHours());
                    assertEquals(3.0, courseResponseModel.getNumCredits());
                    assertEquals("Computer Science", courseResponseModel.getDepartment());
                });

        StepVerifier.create(courseRepository.findCourseByCourseId(existingCourseId))
                .expectNextCount(0)
                .verifyComplete();
    }
}
