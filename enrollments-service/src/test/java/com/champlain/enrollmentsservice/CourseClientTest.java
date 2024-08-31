//package com.champlain.enrollmentsservice;
//
//import com.champlain.enrollmentsservice.domainclientlayer.Courses.CourseClient;
//import com.champlain.enrollmentsservice.domainclientlayer.Courses.CourseResponseModel;
//import com.champlain.enrollmentsservice.utils.exceptions.NotFoundException;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//public class CourseClientTest {
//
//    private final WebClient webClient = Mockito.mock(WebClient.class);
//    private final CourseClient courseClient = new CourseClient("localhost", "8080");
//
//    @Test
//    void testNotFoundException() {
//        Mockito.when(webClient.get().uri("/api/v1/courses/{courseId}", "invalid-id"))
//                .thenThrow(new NotFoundException("Course not found"));
//
//        Mono<CourseResponseModel> result = courseClient.getCourseByCourseId("invalid-id");
//
//        StepVerifier.create(result)
//                .expectError(NotFoundException.class)
//                .verify();
//    }
//}
