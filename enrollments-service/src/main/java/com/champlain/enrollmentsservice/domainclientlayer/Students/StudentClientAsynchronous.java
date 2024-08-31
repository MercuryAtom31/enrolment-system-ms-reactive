package com.champlain.enrollmentsservice.domainclientlayer.Students;

import com.champlain.enrollmentsservice.utils.HttpErrorInfo;
import com.champlain.enrollmentsservice.utils.exceptions.InvalidInputException;
import com.champlain.enrollmentsservice.utils.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.naming.ServiceUnavailableException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Thread.currentThread;

@Service
@Slf4j
public class StudentClientAsynchronous {

    private final WebClient webClient;

    private final String studentClientServiceBaseURL;

private final List<Integer> range = IntStream.rangeClosed(1, 1000)
        .boxed()
        .collect(Collectors.toList());

    public StudentClientAsynchronous(@Value("${app.students-service.host}") String studentsServiceHost,
                                     @Value("${app.students-service.port}") String studentsServicePort) {
        studentClientServiceBaseURL = "http://" + studentsServiceHost + ":" + studentsServicePort + "/api/v1/students";

        this.webClient = WebClient.builder()
                .baseUrl(studentClientServiceBaseURL)
                .build();
    }

//    public Mono<StudentResponseModel> getStudentByStudentId(String studentId) {
//        return webClient.get()
//                .uri(studentClientServiceBaseURL + "/{studentId}", studentId)
//                .retrieve()
//                .onStatus(HttpStatusCode::isError,
//                        error -> switch (error.statusCode().value()) {
//                            case 404 -> Mono.error(new NotFoundException("StudentId not found: " + studentId));
//                            case 422 -> Mono.error(new InvalidInputException("StudentId invalid: " + studentId));
//                            default -> Mono.error(new IllegalArgumentException("Something went wrong"));
//                        })
////                .onStatus(HttpStatusCode::is4xxClientError, error -> {
////                    HttpStatusCode statusCode = error.statusCode();
////                    //add any statuses thrown by lower-level microservices
////                   if (statusCode.equals(HttpStatus.NOT_FOUND)) {
////                        log.debug("404 exception in StudentClientAsynchronous");
////                        return Mono.error(new NotFoundException("StudentId not found: " + studentId));
////                    }
////                    if (statusCode.equals(HttpStatusCode.valueOf(422))) //add any statuses thrown by lower-level microservices
////                        return Mono.error(new InvalidInputException("StudentId invalid: " + studentId));
////                    return Mono.error(new IllegalArgumentException("Something went wrong"));
////                })
//                .bodyToMono(StudentResponseModel.class);
//    }
public Mono<StudentResponseModel> getStudentByStudentId(String studentId) {
    return webClient.get()
            .uri(studentClientServiceBaseURL + "/{studentId}", studentId)
            .retrieve()
            .onStatus(HttpStatusCode::isError, httpErrorInfo ->
                    httpErrorInfo.bodyToMono(HttpErrorInfo.class)
                            .flatMap(error -> {
                                switch (httpErrorInfo.statusCode().value()) {
                                    case 404: return Mono.error(new NotFoundException("Student not found: " + error.getMessage()));
                                    case 422: return Mono.error(new InvalidInputException("Invalid input: " + error.getMessage()));
                                    default: return Mono.error(new IllegalArgumentException("Unexpected error: " + error.getMessage()));
                                }
                            })
            )
            .bodyToMono(StudentResponseModel.class);
}


    /*
       The number of default threads is dependent on the core count of the host system.
       Remember, Webflux will try to keep said threads as busy as possible, so how many
       threads you have assigned doesn't really matter, as long as they consume the full power of the CPU.

       More threads will just have to wait for their turn to use the CPU.

       !! I have 10 cores (12 logical), but I only see 4 being used.
       !! Note my wsl is set to use 6 virtual processors.
     */


    public Flux<StudentResponseModel> getAllStudents() {
        return this.webClient
            .get()
            .uri(studentClientServiceBaseURL)
                .retrieve()
            .bodyToFlux(StudentResponseModel.class);
    }

    //Gets students by databaseRowId
    public Mono<StudentResponseModel> getStudent(int id) {
   //log.debug(String.format("Calling getStudentAsync(%d)", id));


        return webClient.get()
                .uri(studentClientServiceBaseURL + "/row/{id}", id)
                .retrieve()
                .bodyToMono(StudentResponseModel.class);

    }
    public Flux<StudentResponseModel> get1000StudentsAsync() {

        /* We invoke flatMap to run the getUser method we created previously.
        This reactive operator has a concurrency level of 256 (on one thread) by default,
        meaning it executes at most 256 getUser calls simultaneously.
        This number is configurable via method parameter using an overloaded version of flatMap.
         */

        /* Snippet of results from running this method: all are on same io thread

enrollments-service  | 2024-08-13T19:31:55.520Z  INFO 1 --- [enrollments-service] [or-http-epoll-2] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#35,reactor-http-epoll-2,5,main]
enrollments-service  | 2024-08-13T19:31:55.520Z  INFO 1 --- [enrollments-service] [or-http-epoll-2] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#35,reactor-http-epoll-2,5,main]
enrollments-service  | 2024-08-13T19:31:55.520Z  INFO 1 --- [enrollments-service] [or-http-epoll-2] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#35,reactor-http-epoll-2,5,main]
enrollments-service  | 2024-08-13T19:31:55.520Z  INFO 1 --- [enrollments-service] [or-http-epoll-2] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#35,reactor-http-epoll-2,5,main]
enrollments-service  | 2024-08-13T19:31:55.520Z  INFO 1 --- [enrollments-service] [or-http-epoll-2] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#35,reactor-http-epoll-2,5,main]

         */

        return Flux.fromIterable(range)
                .flatMap(this::getStudent)
                .doOnNext(s -> log.info("Current thread running " + currentThread()));
    }

    public Flux<StudentResponseModel> get1000StudentsAsyncParallel() {

        /* Snippet of results from running this method: up to max cores of io threads and multiple parallel threads (system and load dependant)

enrollments-service              | 2024-08-20T16:44:49.442Z  INFO 1 --- [enrollments-service] [or-http-epoll-3] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#46,reactor-http-epoll-3,5,main]
enrollments-service              | 2024-08-20T16:44:49.460Z  INFO 1 --- [enrollments-service] [     parallel-1] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#56,parallel-1,5,main]
enrollments-service              | 2024-08-20T16:44:49.466Z  INFO 1 --- [enrollments-service] [     parallel-1] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#56,parallel-1,5,main]
enrollments-service              | 2024-08-20T16:44:49.469Z  INFO 1 --- [enrollments-service] [     parallel-1] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#56,parallel-1,5,main]
enrollments-service              | 2024-08-20T16:44:49.473Z  INFO 1 --- [enrollments-service] [     parallel-1] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#56,parallel-1,5,main]
enrollments-service              | 2024-08-20T16:44:49.475Z  INFO 1 --- [enrollments-service] [     parallel-1] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#56,parallel-1,5,main]
enrollments-service              | 2024-08-20T16:44:49.476Z  INFO 1 --- [enrollments-service] [     parallel-1] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#56,parallel-1,5,main]
enrollments-service              | 2024-08-20T16:44:49.479Z  INFO 1 --- [enrollments-service] [     parallel-1] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#56,parallel-1,5,main]
enrollments-service              | 2024-08-20T16:44:49.483Z  INFO 1 --- [enrollments-service] [or-http-epoll-5] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#54,reactor-http-epoll-5,5,main]
enrollments-service              | 2024-08-20T16:44:49.497Z  INFO 1 --- [enrollments-service] [or-http-epoll-5] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#54,reactor-http-epoll-5,5,main]
enrollments-service              | 2024-08-20T16:44:49.503Z  INFO 1 --- [enrollments-service] [or-http-epoll-5] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#54,reactor-http-epoll-5,5,main]

         */

       return Flux.fromIterable(range)
                .flatMap(this::getStudent)
                .doOnNext(s -> log.info("Current thread running " + currentThread()))
                .subscribeOn(Schedulers.parallel());
    }

    public Flux<StudentResponseModel> get1000StudentsAsyncBounded() {

        /* Snippet of results from running this method: 1 bounded elastic thread, up to max cores of io threads

enrollments-service  | 2024-08-13T19:27:24.605Z  INFO 1 --- [enrollments-service] [oundedElastic-1] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#37,boundedElastic-1,5,main]
enrollments-service  | 2024-08-13T19:27:24.605Z  INFO 1 --- [enrollments-service] [oundedElastic-1] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#37,boundedElastic-1,5,main]
enrollments-service  | 2024-08-13T19:27:24.606Z  INFO 1 --- [enrollments-service] [oundedElastic-1] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#37,boundedElastic-1,5,main]
enrollments-service  | 2024-08-13T19:27:24.606Z  INFO 1 --- [enrollments-service] [oundedElastic-1] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#37,boundedElastic-1,5,main]
enrollments-service  | 2024-08-13T19:27:24.606Z  INFO 1 --- [enrollments-service] [oundedElastic-1] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#37,boundedElastic-1,5,main]
enrollments-service  | 2024-08-13T19:27:24.609Z  INFO 1 --- [enrollments-service] [or-http-epoll-6] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#40,reactor-http-epoll-6,5,main]
enrollments-service  | 2024-08-13T19:27:24.609Z  INFO 1 --- [enrollments-service] [or-http-epoll-6] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#40,reactor-http-epoll-6,5,main]
enrollments-service  | 2024-08-13T19:27:24.610Z  INFO 1 --- [enrollments-service] [or-http-epoll-6] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#40,reactor-http-epoll-6,5,main]
enrollments-service  | 2024-08-13T19:27:24.611Z  INFO 1 --- [enrollments-service] [or-http-epoll-6] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#40,reactor-http-epoll-6,5,main]
enrollments-service  | 2024-08-13T19:27:24.612Z  INFO 1 --- [enrollments-service] [or-http-epoll-6] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#40,reactor-http-epoll-6,5,main]
enrollments-service  | 2024-08-13T19:27:24.617Z  INFO 1 --- [enrollments-service] [or-http-epoll-6] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#40,reactor-http-epoll-6,5,main]
enrollments-service  | 2024-08-13T19:27:24.619Z  INFO 1 --- [enrollments-service] [or-http-epoll-2] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#35,reactor-http-epoll-2,5,main]
enrollments-service  | 2024-08-13T19:27:24.621Z  INFO 1 --- [enrollments-service] [or-http-epoll-4] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#38,reactor-http-epoll-4,5,main]
enrollments-service  | 2024-08-13T19:27:24.622Z  INFO 1 --- [enrollments-service] [or-http-epoll-4] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#38,reactor-http-epoll-4,5,main]
enrollments-service  | 2024-08-13T19:27:24.622Z  INFO 1 --- [enrollments-service] [or-http-epoll-4] c.c.e.d.StudentClientAsynchronous        : Current thread running Thread[#38,reactor-http-epoll-4,5,main]

         */


        return Flux.fromIterable(range)
                .flatMap(this::getStudent)
                .doOnNext(s -> log.info("Current thread running " + currentThread()))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
