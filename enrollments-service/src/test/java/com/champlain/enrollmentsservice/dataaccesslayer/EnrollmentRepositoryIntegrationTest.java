package com.champlain.enrollmentsservice.dataaccesslayer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataMongoTest
@ActiveProfiles("test")
public class EnrollmentRepositoryIntegrationTest {

    @Autowired
    private EnrollmentRepository enrollmentRepository;
    Enrollment enrollment1 = Enrollment.builder()
            .enrollmentId("06a7d573-bcab-4db3-956f-773324b92a80")
            .enrollmentYear(2021)
            .semester(Semester.FALL)
            .studentId("c3540a89-cb47-4c96-888e-ff96708db4d8")
            .studentFirstName("Christine")
            .studentLastName("Gerard")
            .courseId("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223")
            .courseNumber("trs-075")
            .courseName("Web Services")
            .build();
    Enrollment enrollment2 = Enrollment.builder()
            .enrollmentId("98f7b33a-d62a-420a-a84a-05a27c85fc91")
            .enrollmentYear(2021)
            .semester(Semester.FALL)
            .studentId("c3540a89-cb47-4c96-888e-ff96708db4d8")
            .studentFirstName("Christine")
            .studentLastName("Gerard")
            .courseId("d819e4f4-25af-4d33-91e9-2c45f0071606")
            .courseNumber("ygo-675")
            .courseName("Shakespeare's Greatest Works")
            .build();

    @BeforeEach
    public void setupDB() {
        Publisher<Enrollment> setupDB = enrollmentRepository.deleteAll()
                .thenMany(Flux.just(enrollment1, enrollment2))
                .flatMap(enrollmentRepository::save);
        StepVerifier.create(setupDB)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findEnrollmentByEnrollmentId_withExistingId_shouldReturnEnrollment() {
        StepVerifier.create(enrollmentRepository.findEnrollmentByEnrollmentId(enrollment1.getEnrollmentId()))
                .consumeNextWith(foundEnrollment -> {
                    assertNotNull(foundEnrollment);
                    assertEquals(enrollment1.getEnrollmentId(),
                            foundEnrollment.getEnrollmentId());
        })
                .verifyComplete();
    }

    @Test
    void findEnrollmentByEnrollmentId_withNonExistingId_shouldReturnEmptyMono() {
        String nonExistingEnrollmentId = "non-existing-id";

        StepVerifier.create(enrollmentRepository.findEnrollmentByEnrollmentId(nonExistingEnrollmentId))
                .expectNextCount(0)  // Since it's a Mono, we expect no values
                .verifyComplete();
    }
}
