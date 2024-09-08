package com.champlain.courseservice.businesslayer;

import com.champlain.courseservice.dataaccesslayer.Course;
import com.champlain.courseservice.dataaccesslayer.CourseRepository;
import com.champlain.courseservice.presentationlayer.CourseRequestModel;
import com.champlain.courseservice.presentationlayer.CourseResponseModel;
import com.champlain.courseservice.utils.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeanUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * StepVerifier from Reactor is used to verify the reactive responses.
 * BeanUtils is used for copying properties between objects
 * (e.g., copying properties from Course to CourseRequestModel).
 */

/**
 * //@ExtendWith(MockitoExtension.class)
 * This annotation enables Mockito extension,
 * which allows for mocking and injecting dependencies into the unit test.
 */
@ExtendWith(MockitoExtension.class)//For mocking the service.
public class CourseServiceUnitTest {

    @InjectMocks //Tells Mockito to inject mocked dependencies into the courseService.
    private CourseServiceImpl courseService;

    /*
    Mocks the CourseRepository, which simulates the repository behavior
    without actually interacting with a real database.
     */
    @Mock
    private CourseRepository courseRepository;

    /**
     * Three Course objects (course1, course2, course3)
     * are created using the builder pattern.
     * These are used as test data in the unit tests.
     * <p>
     * The Builder Pattern is a creational design pattern that allows for the
     * step-by-step construction of complex objects.
     */
    Course course1 = Course.builder()
            .id(1)
            .courseId(UUID.randomUUID().toString())
            .courseNumber("cat-420")
            .courseName("Web Services")
            .numHours(45)
            .numCredits(3.0)
            .department("Computer Science")
            .build();

    Course course2 = Course.builder()
            .id(2)
            .courseId(UUID.randomUUID().toString())
            .courseNumber("cat-421")
            .courseName("Advanced Web Services")
            .numHours(45)
            .numCredits(3.0)
            .department("Computer Science")
            .build();

    Course course3 = Course.builder()
            .id(3)
            .courseId(UUID.randomUUID().toString())
            .courseNumber("cat-422")
            .courseName("Web Service Security")
            .numHours(45)
            .numCredits(3.0)
            .department("Computer Science")
            .build();

    @Test
    void addCourse_shouldReturnCourseResponseModel() {

        /**
         * This mocks the save method in courseRepository to return a
         * Mono Course containing course1 when it's called with any Course object.
         * "When it's called with any Course object"
         * any(Course.class) is a matcher used in Mockito, which is a tool
         * that helps simulate method calls in tests.
         * It means that no matter what specific Course object is passed to
         * the save method during the test, the fake method will behave the same way.
         * It doesn't care what exact Course object is being passed;
         * it will always return the predefined response (a Mono containing course1).
         */
        when(courseRepository.save(any(Course.class)))
                .thenReturn(Mono.just(course1));
        /**
         * This creates a CourseRequestModel object
         * (which represents the data sent to the service when adding a course).
         * The BeanUtils.copyProperties method copies the properties of course1
         * into courseRequestModel, ensuring that the two objects contain
         * the same data.
         */
        CourseRequestModel courseRequestModel = new CourseRequestModel();
        BeanUtils.copyProperties(course1, courseRequestModel);

        Mono<CourseResponseModel> result =
                courseService.addCourse(Mono.just(courseRequestModel));

        StepVerifier
                .create(result)
                /*
                expectNextMatches checks that the next emitted value
                (the CourseResponseModel) satisfies certain conditions.

                .expectNextMatches() is used to check if the next item in
                a sequence matches certain conditions.
                 */
                .expectNextMatches(courseResponseModel -> {
                    System.out.println(courseResponseModel);
                    /*
                    The courseId should not be null (i.e., the course was properly created).
                     */
                    assertNotNull(courseResponseModel.getCourseId());
                    /*
                    The courseNumber in the CourseResponseModel should match the courseNumber from the CourseRequestModel.
                     */
                    assertEquals(courseResponseModel.getCourseNumber(),
                            courseRequestModel.getCourseNumber());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void whenGetAllCourses_thenReturnAllCourses() {
        //make sure this is a Mockito when
        when(courseRepository.findAll())
                .thenReturn(Flux.just(course1, course2, course3));
        //act
        Flux<CourseResponseModel> result = courseService.getAllCourses();
        //assert
        StepVerifier.create(result)
                .expectNextMatches(courseResponseModel -> {
                    assertNotNull(courseResponseModel.getCourseId());
                    assertEquals(courseResponseModel.getCourseNumber(),
                            course1.getCourseNumber());
                    return true;
                })
                .expectNextMatches(courseResponseModel ->
                        courseResponseModel.getCourseNumber().equals("cat-421"))
                .expectNextMatches(courseResponseModel ->
                        courseResponseModel.getCourseNumber().equals("cat-422"))
                .verifyComplete();
    }

    @Test
    void getCourseByCourseId_withExistingId_thenReturnCourseResponseModel() {
        // Arrange
        // Use course1 as the mock data for the test
        String courseId = course1.getCourseId();
        when(courseRepository.findCourseByCourseId(courseId))
                // Mock repository to return course1 when the courseId matches
                .thenReturn(Mono.just(course1));

        // Act
        Mono<CourseResponseModel> result = courseService.getCourseByCourseId(courseId);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(courseResponseModel -> {
                    assertNotNull(courseResponseModel.getCourseId());
                    // Verifying courseIDs match
                    assertEquals(courseResponseModel.getCourseId(), courseId);
                    assertEquals(courseResponseModel.getCourseName(), course1.getCourseName());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void getCourseByCourseId_withNonExistingId_thenThrowNotFoundException() {

        // Arrange
        String nonExistingCourseId = UUID.randomUUID().toString();
        when(courseRepository.findCourseByCourseId(nonExistingCourseId))
                // Mock the repository to return an empty Mono for the non-existing courseId
                /*
                Simulating "not found" behavior:
                If the repository is asked to find a course with a courseId
                that doesn't exist, it should return an empty result.
                Mono.empty() models this behavior by returning a Mono that contains
                no data, signaling that the course wasn't found.
                 */
                .thenReturn(Mono.empty());

        // Act
        Mono<CourseResponseModel> result = courseService.getCourseByCourseId(nonExistingCourseId);

        // Assert
        StepVerifier
                .create(result)
                /*
                This checks whether the throwable (the error that occurred)
                is an instance of the NotFoundException class.
                 */
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException)
                .verify(); // Verify that the exception is a NotFoundException
    }

    @Test
    void updateCourseByCourseId_withExistingCourseId_thenReturnUpdatedCourseResponseModel() {
        // Arrange
        String courseId = course1.getCourseId();

        when(courseRepository.findCourseByCourseId(courseId))
                .thenReturn(Mono.just(course1));

        // Create a modified version of course1 with updated details
        Course updatedCourse = Course.builder()
                .id(course1.getId())
                .courseId(courseId)
                .courseNumber("cat-999")
                .courseName("Updated Web Services")
                .numHours(50)
                .numCredits(4.0)
                .department("Updated Department")
                .build();

        // Mock (imitate) the save operation to return the updated course
        when(courseRepository.save(any(Course.class)))
                .thenReturn(Mono.just(updatedCourse));

        // Create CourseRequestModel for the updated course
        CourseRequestModel updatedRequestModel = new CourseRequestModel();
        BeanUtils.copyProperties(updatedCourse, updatedRequestModel);

        // Act
        Mono<CourseResponseModel> result = courseService.updateCourseByCourseId(Mono.just(updatedRequestModel), courseId);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(courseResponseModel -> {
                    assertNotNull(courseResponseModel.getCourseId());
                    assertEquals(courseResponseModel.getCourseId(), courseId);
                    assertEquals(courseResponseModel.getCourseNumber(), "cat-999");
                    assertEquals(courseResponseModel.getCourseName(), "Updated Web Services");
                    assertEquals(courseResponseModel.getNumHours(), 50);
                    assertEquals(courseResponseModel.getNumCredits(), 4.0);
                    assertEquals(courseResponseModel.getDepartment(), "Updated Department");
                    return true; // Indicate that the conditions were met
                })
                .verifyComplete();
    }


    @Test
    void updateCourseByCourseId_withNonExistingCourseId_thenThrowNotFoundException() {

        //Arrange
        String nonExistingCourseId = UUID.randomUUID().toString();

        when(courseRepository.findCourseByCourseId(nonExistingCourseId))
                .thenReturn(Mono.empty());

        CourseRequestModel courseRequestModel = new CourseRequestModel();

        //Act
        Mono<CourseResponseModel> result = courseService.updateCourseByCourseId(Mono.just(courseRequestModel),
                nonExistingCourseId);

        //Assert
        StepVerifier
                .create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException)
                .verify();

    }

    @Test
    void deleteCourseByCourseId_withExistingCourseId_ReturnsDeletedCourseId() {

        // Arrange
        String courseId = course1.getCourseId();

        // Mock(imitate) the repository to return the course when the courseId is found
        when(courseRepository.findCourseByCourseId(courseId))
                .thenReturn(Mono.just(course1)); // Simulate finding the course

        when(courseRepository.delete(course1))
                .thenReturn(Mono.empty());

        // Act
        Mono<CourseResponseModel> result = courseService.deleteCourseByCourseId(courseId);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(courseResponseModel -> {
                    assertNotNull(courseResponseModel.getCourseId());
                    assertEquals(courseResponseModel.getCourseId(), courseId);
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void deleteCourseByCourseId_withNonExistingCourseId_thenThrowNotFoundException() {
        // Arrange
        String nonExistingCourseId = UUID.randomUUID().toString();

        // Mock (imitate) the repository to return an empty Mono when the courseId is not found
        when(courseRepository.findCourseByCourseId(nonExistingCourseId))
                .thenReturn(Mono.empty());

        // Act
        Mono<CourseResponseModel> result = courseService.deleteCourseByCourseId(nonExistingCourseId);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof NotFoundException)
                .verify();
    }
}
