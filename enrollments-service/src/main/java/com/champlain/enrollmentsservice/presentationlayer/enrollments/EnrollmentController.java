package com.champlain.enrollmentsservice.presentationlayer.enrollments;

import com.champlain.enrollmentsservice.businesslayer.enrollments.EnrollmentService;
import com.champlain.enrollmentsservice.utils.exceptions.InvalidInputException;
import com.champlain.enrollmentsservice.utils.exceptions.NotFoundException;
import io.micrometer.core.ipc.http.HttpSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/enrollments")
@Slf4j
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;

    }

    @PostMapping
    public Mono<ResponseEntity<EnrollmentResponseModel>> addEnrollment(@RequestBody Mono<EnrollmentRequestModel> enrollmentRequestModel) {
        return enrollmentService.addEnrollment(enrollmentRequestModel)
                .map(e -> ResponseEntity.status(HttpStatus.CREATED).body(e))
                .defaultIfEmpty(ResponseEntity.badRequest().build());
    }

    @GetMapping(value = "", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<EnrollmentResponseModel> getAllEnrollments(){
        return enrollmentService.getAllEnrollments();
    }

    @GetMapping(value = "/{enrollmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<EnrollmentResponseModel>> getEnrollmentByEnrollmentId(@PathVariable String enrollmentId) {
        return Mono.just(enrollmentId)
                .filter(id -> id.length() == 36)
                .switchIfEmpty(Mono.error(new InvalidInputException("Provided Enrollment ID is invalid: " + enrollmentId)))
                .flatMap(enrollmentService::getEnrollmentByEnrollmentId)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping(value = "/{enrollmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<EnrollmentResponseModel>> deleteEnrollmentByEnrollmentId(@PathVariable String enrollmentId){
        return Mono.just(enrollmentId)
                .filter(id -> id.length() == 36) //validate the course id
                .switchIfEmpty(Mono.error(
                        new InvalidInputException
                                ("Provided Enrollment ID is invalid: " + enrollmentId)))
                .flatMap(enrollmentService::deleteEnrollmentByEnrollmentId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.badRequest()
                        .build());
    }

    @PutMapping(value = "/{enrollmentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<EnrollmentResponseModel>> updateEnrollmentByEnrollmentId(@RequestBody Mono<EnrollmentRequestModel> enrollmentRequestModel, @PathVariable String enrollmentId){
        return enrollmentService.updateEnrollmentByEnrollmentId(enrollmentRequestModel, enrollmentId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
