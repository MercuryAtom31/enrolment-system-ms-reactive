package com.champlain.enrollmentsservice.domainclientlayer.Courses;

import com.champlain.enrollmentsservice.utils.HttpErrorInfo;
import com.champlain.enrollmentsservice.utils.exceptions.InvalidInputException;
import com.champlain.enrollmentsservice.utils.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class CourseClient {

    private final WebClient webClient;

    public CourseClient(@Value("${app.courses-service.host}") String coursesServiceHost,
                        @Value("${app.courses-service.port}") String coursesServicePort) {
        String courseClientServiceBaseURL = "http://" + coursesServiceHost + ":" + coursesServicePort + "/api/v1/courses";

        this.webClient = WebClient.builder()
                .baseUrl(courseClientServiceBaseURL)
                .build();
    }

    public Mono<CourseResponseModel> getCourseByCourseId(final String courseId) {
        return webClient.get()
                .uri("/{courseId}", courseId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, error ->
                        switch (error.statusCode().value()) {
                            case 404 -> Mono.error(new NotFoundException("CourseId not found: " + courseId));
                            case 422 -> Mono.error(new InvalidInputException("CourseId invalid: " + courseId));
                            default -> Mono.error(new IllegalArgumentException("Something went wrong"));
                        })
                .bodyToMono(CourseResponseModel.class);
    }


    // Add more methods here if needed, ensuring they follow the same exception handling pattern
}


//package com.champlain.enrollmentsservice.domainclientlayer.Courses;
//
//import com.champlain.enrollmentsservice.utils.HttpErrorInfo;
//import com.champlain.enrollmentsservice.utils.exceptions.InvalidInputException;
//import com.champlain.enrollmentsservice.utils.exceptions.NotFoundException;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatusCode;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//@Service
//public class CourseClient {
//
//    private final WebClient webClient;
//
////    public CourseClient(@Value("${app.courses-service.host}") String coursesServiceHost,
////                         @Value("${app.courses-service.port}") String coursesServicePort) {
////        courseClientServiceBaseURL = "http://" + coursesServiceHost + ":" + coursesServicePort + "/api/v1/courses";
////
////        this.webClient = WebClient.builder()
////                .baseUrl(courseClientServiceBaseURL)
////                .build();
////    }
//
//    public CourseClient(@Value("${app.courses-service.host}") String coursesServiceHost,
//                        @Value("${app.courses-service.port}") String coursesServicePort) {
//        //    private final String courseClientServiceBaseURL;
//        String courseClientServiceBaseURL = "http://" + coursesServiceHost + ":" + coursesServicePort + "/api/v1/courses";
//
//        this.webClient = WebClient.builder()
//                .baseUrl(courseClientServiceBaseURL)
//                .build();
//    }
//
//    public Mono<CourseResponseModel> getCourseByCourseId(final String courseId) {
//        return webClient.get()
//                .uri("/{courseId}", courseId)
//                .retrieve()
//                .onStatus(HttpStatusCode::isError,
//                        httpErrorInfo -> httpErrorInfo
//                                .bodyToMono(HttpErrorInfo.class)
//                                .flatMap(error ->
//                                {
//                                    switch (httpErrorInfo.statusCode().value())
//                                    { case 404: return Mono.error(new NotFoundException(error.getMessage()));
//                                        case 422: return Mono.error(new InvalidInputException(error.getMessage()));
//                                        default: return Mono.error(new IllegalArgumentException(error.getMessage()));
//                                    }
//                                })
//                ) .bodyToMono(CourseResponseModel.class);
//    }
//
//}
