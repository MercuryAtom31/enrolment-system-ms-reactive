package com.champlain.courseservice.dataaccesslayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * This class tests the integration of your CourseRepository
 * with the database using the R2DBC
 * (Reactive Relational Database Connectivity) framework.
 */
@DataR2dbcTest
@ActiveProfiles("test")
public class CourseRepositoryIntegrationTest {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private DatabaseClient databaseClient;

    /**
     * This method runs before each test to clear all records from the CourseRepository.
     * This ensures that each test starts with an empty database.
     */
    @BeforeEach
    public void setUpDB(){
        StepVerifier
                .create(courseRepository.deleteAll())
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void findCourseByCourseId_shouldSucceedWhenExistingId(){
        //arrange
        String courseId = UUID.randomUUID().toString();
        Course course = Course.builder()
                .courseId(courseId)
                .courseNumber("cat-420")
                .courseName("Web Services")
                .numHours(45)
                .numCredits(3.0)
                .department("Computer Science")
                .build();

        StepVerifier
                .create(courseRepository.save(course))
                /**
                 * .consumeNextWith(), it takes the next value that is emitted by the stream
                 * (in this case, the course that was saved or found)
                 * and allows you to do something with it,
                 * like checking if it's correct or meets certain conditions.
                 */
                .consumeNextWith(insertedCourse -> {
                    assertNotNull(insertedCourse);
                    assertEquals(course.getCourseId(), insertedCourse.getCourseId());
                })
                .verifyComplete();

        StepVerifier
                .create(courseRepository
                        .findCourseByCourseId(course.getCourseId()))
                .consumeNextWith(foundCourse -> {
                    assertNotNull(foundCourse);
                    assertEquals(course.getCourseId(), foundCourse.getCourseId());
                })
                .verifyComplete();
    }

    @Test
    void findCourseByCourseId_shouldFailWhenNonExistingId(){
        // Arrange
        String nonExistingCourseId = UUID.randomUUID().toString();

        // Act & Assert
        StepVerifier
                .create(courseRepository.findCourseByCourseId(nonExistingCourseId))
                .expectNextCount(0)  // Expecting no values to be emitted (since the course doesn't exist)
                .verifyComplete();
    }
}
