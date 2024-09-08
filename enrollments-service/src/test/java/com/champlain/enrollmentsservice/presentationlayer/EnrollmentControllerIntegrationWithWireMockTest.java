package com.champlain.enrollmentsservice.presentationlayer;

import com.champlain.enrollmentsservice.MockServerConfigCoursesService;
import com.champlain.enrollmentsservice.MockServerConfigStudentsService;
import com.champlain.enrollmentsservice.dataaccesslayer.Enrollment;
import com.champlain.enrollmentsservice.dataaccesslayer.EnrollmentRepository;
import com.champlain.enrollmentsservice.dataaccesslayer.Semester;
import com.champlain.enrollmentsservice.domainclientlayer.Courses.CourseResponseModel;
import com.champlain.enrollmentsservice.domainclientlayer.Students.StudentResponseModel;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentRequestModel;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentResponseModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
@ActiveProfiles("test")
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureWebTestClient
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EnrollmentControllerIntegrationWithWireMockTest {


    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    private MockServerConfigStudentsService mockServerConfigStudentsService;
    private MockServerConfigCoursesService mockServerConfigCoursesService;

    //Added these fields.
    private static final String NON_EXISTING_STUDENTID = "154tr995-fs94-4th1-5rr5-8f6a571fz0bb";
    private static final String INVALID_STUDENTID = "Student123";

    private static final String NON_EXISTING_COURSEID = "206bf934-rf95-4hh1-9fg5-6h6a341fb3zz";
    private static final String INVALID_COURSEID = "Course123";

    //End of added fields.
    private final Long dbSize = 2L;
    private final String NON_EXISTING_ENROLLMENTID = "169de995-ef94-4dd1-9ea9-8f6a571fb0dd";
    private final String INVALID_ENROLLMENTID = "Enrollment123";

    private StudentResponseModel student1ResponseModel = StudentResponseModel.builder()
            .studentId("c3540a89-cb47-4c96-888e-ff96708db4d8")
            .firstName("Donna")
            .lastName("Hornsby")
            .program("History")
            .stuff("stuff")
            .build();

    //for update request
    private StudentResponseModel student2ResponseModel = StudentResponseModel.builder()
            .studentId("1f538db7-320a-4415-bad4-e1d44518b1ff")
            .firstName("Willis")
            .lastName("Faraday")
            .program("Pure and Applied Sciences")
            .stuff("stuff")
            .build();

    private CourseResponseModel course1ResponseModel = CourseResponseModel.builder()
            .courseId("9a29fff7-564a-4cc9-8fe1-36f6ca9bc223")
            .courseName("Web Services")
            .courseNumber("N45-LA")
            .department("Computer Science")
            .numCredits(2.0)
            .numHours(60)
            .build();

    //for update request
    private CourseResponseModel course2ResponseModel = CourseResponseModel.builder()
            .courseId("8d764f78-8468-4769-b643-10cde392fbde")
            .courseName("Waves")
            .courseNumber("xud-857")
            .department("Physics")
            .numCredits(2.5)
            .numHours(60)
            .build();

    private EnrollmentRequestModel enrollment1RequestModel = EnrollmentRequestModel.builder()
            .enrollmentYear(2021)
            .semester(Semester.FALL)
            .studentId(student1ResponseModel.getStudentId())
            .courseId(course1ResponseModel.getCourseId())
            .build();

    //for update request
    private EnrollmentRequestModel enrollment2RequestModel = EnrollmentRequestModel.builder()
            .enrollmentYear(2023)
            .semester(Semester.FALL)
            .studentId(student2ResponseModel.getStudentId())
            .courseId(course2ResponseModel.getCourseId())
            .build();

    //for non-existing studentId
    private EnrollmentRequestModel enrollment_withNonExistingStudentId_RequestModel =
            EnrollmentRequestModel.builder()
                    .enrollmentYear(2023)
                    .semester(Semester.FALL)
                    .studentId(NON_EXISTING_STUDENTID)
                    .courseId(course2ResponseModel.getCourseId())
                    .build();

    //for invalid studentId
    private EnrollmentRequestModel enrollment_withInvalidStudentId_RequestModel =
            EnrollmentRequestModel.builder()
                    .enrollmentYear(2023)
                    .semester(Semester.FALL)
                    .studentId(INVALID_STUDENTID)
                    .courseId(course2ResponseModel.getCourseId())
                    .build();

    //for non-existing courseId
    private EnrollmentRequestModel enrollment_withNonExistingCourseId_RequestModel =
            EnrollmentRequestModel.builder()
                    .enrollmentYear(2023)
                    .semester(Semester.FALL)
                    .studentId(student1ResponseModel.getStudentId())
                    .courseId(NON_EXISTING_COURSEID)
                    .build();

    //for invalid courseId
    private EnrollmentRequestModel enrollment_withInvalidCourseId_RequestModel =
            EnrollmentRequestModel.builder()
                    .enrollmentYear(2023)
                    .semester(Semester.FALL)
                    .studentId(student1ResponseModel.getStudentId())
                    .courseId(INVALID_COURSEID)
                    .build();

    //for database initialization
    private Enrollment enrollment1 = Enrollment.builder()
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

    private Enrollment enrollment2 = Enrollment.builder()
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

    @BeforeAll
    public void startServers() {
        mockServerConfigStudentsService = new MockServerConfigStudentsService();
        mockServerConfigStudentsService.registerGetStudent1ByStudentIdEndpoint();
        mockServerConfigStudentsService.registerGetStudent2ByStudentIdEndpoint();

        mockServerConfigStudentsService.registerGetStudent_NonExisting_ByStudentIdEndpoint();

        mockServerConfigStudentsService.registerGetStudent_INVALID_ByStudentIdEndpoint();

        mockServerConfigCoursesService = new MockServerConfigCoursesService();
        mockServerConfigCoursesService.registerGetCourse1ByCourseIdEndpoint();
        mockServerConfigCoursesService.registerGetCourse2ByCourseIdEndpoint();
        mockServerConfigCoursesService.registerGetCourse_NonExisting_ByCourseIdEndpoint();
        mockServerConfigCoursesService.registerGetCourse_INVALID_ByCourseIdEndpoint();

    }

    @BeforeEach
    public void setupDB() {
        Publisher<Enrollment> setupDB = enrollmentRepository.deleteAll()
                .thenMany(Flux.just(enrollment1, enrollment2))
                .flatMap(enrollmentRepository::save);
        StepVerifier.create(setupDB)
                .expectNextCount(2)
                .verifyComplete();
    }

    @AfterEach
    public void tearDownEach() {
        //mockServerConfigStudentsService.verifyGetStudent1ByStudentIdEndpoint();
        // mockServerConfigCoursesService.verifyGetCourse1ByCourseIdEndpoint();
    }

    @AfterAll
    public void tearDown() {
        mockServerConfigStudentsService.stopServer();
        mockServerConfigCoursesService.stopServer();
    }

    @Test
    void whenAddEnrollment_thenReturnEnrollmentResponseModel() {
        //act
        webTestClient.post()
                .uri("/api/v1/enrollments")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(Mono.just(enrollment1RequestModel), EnrollmentRequestModel.class)
                .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isCreated()

                .expectHeader().contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .expectBody(EnrollmentResponseModel.class)
                .value(enrollmentResponseModel -> {
                    assertNotNull(enrollmentResponseModel);
                    assertNotNull(enrollmentResponseModel.getEnrollmentId());
                    assertEquals(enrollment1RequestModel.getEnrollmentYear(),
                            enrollmentResponseModel.getEnrollmentYear());
                    assertEquals(enrollment1RequestModel.getSemester(),
                            enrollmentResponseModel.getSemester());
                    assertEquals(enrollment1RequestModel.getStudentId(),
                            enrollmentResponseModel.getStudentId());
                    assertEquals(student1ResponseModel.getFirstName(),
                            enrollmentResponseModel.getStudentFirstName());
                    assertEquals(student1ResponseModel.getLastName(),
                            enrollmentResponseModel.getStudentLastName());
                    assertEquals(enrollment1RequestModel.getCourseId(),
                            enrollmentResponseModel.getCourseId());
                    assertEquals(course1ResponseModel.getCourseName(),
                            enrollmentResponseModel.getCourseName());
                    assertEquals(course1ResponseModel.getCourseNumber(),
                            enrollmentResponseModel.getCourseNumber());
                });
        // Assert
        StepVerifier
                .create(enrollmentRepository.findAll())
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    public void whenAddEnrollment_withInvalidStudentId_thenThrowUnprocessableEntityException() {
        // Act
        webTestClient.post()
                .uri("/api/v1/enrollments")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(Mono.just(enrollment_withInvalidStudentId_RequestModel),
                        EnrollmentRequestModel.class)
                .accept(org.springframework.http.MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isEqualTo(org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY)
                .expectHeader()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("StudentId invalid: " + INVALID_STUDENTID);
        // Assert
        StepVerifier
                .create(enrollmentRepository.findAll())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void whenAddEnrollment_withNonExistingCourseId_thenThrowNotFoundException() {
        // Act
        webTestClient.post()
                .uri("/api/v1/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(enrollment_withNonExistingCourseId_RequestModel), EnrollmentRequestModel.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.message")
                .isEqualTo("CourseId not found: " + NON_EXISTING_COURSEID);
        // Assert
        StepVerifier
                .create(enrollmentRepository.findAll())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    public void whenAddEnrollment_withNonExistingStudentId_thenThrowNotFoundException() {

    }

    @Test
    public void whenAddEnrollment_withInvalidCourseId_thenThrowUnprocessableEntityException() {

    }

    @Test
    void whenGetEnrollmentByEnrollmentId_withExistingEnrollmentId_thenReturnEnrollmentResponseModel() {

    }

    @Test
    public void whenGetEnrollmentByEnrollmentId_withNonExistingEnrollmentId_thenThrowNotFoundException() {

    }

    @Test
    public void whenGetEnrollmentByEnrollmentId_withInvalidEnrollmentId_thenThrowUnprocessableEntityException() {


    }

    @Test
    public void whenUpdateEnrollment_withExistingEnrollmentId_thenReturnEnrollmentResponseModel() {

    }

    @Test
    public void whenUpdateEnrollment_withNonExistingEnrollmentId_thenThrowNotFoundException() {

    }

    @Test
    public void whenUpdateEnrollment_withInvalidEnrollmentId_thenThrowUnprocessableEntityException() {

    }

    @Test
    public void whenGetAllEnrollments_thenReturnFluxOfTwoEnrollmentResponseModels() {

    }

    @Test
    void whenDeleteEnrollment_withExistingEnrollmentId_thenEnrollmentDeletedAndEnrollmentResponseModelReturned() {

    }

    @Test
    public void whenDeleteEnrollment_withNonExistingEnrollmentId_thenThrowNotFoundException() {

    }

    @Test
    public void whenDeleteEnrollment_withInvalidEnrollmentId_thenThrowUnprocessableEntityException() {

    }
}
