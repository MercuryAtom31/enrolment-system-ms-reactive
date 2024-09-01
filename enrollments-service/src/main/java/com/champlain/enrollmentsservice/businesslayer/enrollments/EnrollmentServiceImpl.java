package com.champlain.enrollmentsservice.businesslayer.enrollments;

import com.champlain.enrollmentsservice.dataaccesslayer.EnrollmentRepository;
import com.champlain.enrollmentsservice.dataaccesslayer.Semester;
import com.champlain.enrollmentsservice.domainclientlayer.Courses.CourseClient;
import com.champlain.enrollmentsservice.domainclientlayer.Students.StudentClientAsynchronous;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentRequestModel;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentResponseModel;
import com.champlain.enrollmentsservice.utils.EntityModelUtil;
import com.champlain.enrollmentsservice.utils.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class EnrollmentServiceImpl implements EnrollmentService{

    private final StudentClientAsynchronous studentClient;
    private final CourseClient courseClient;
    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentServiceImpl(StudentClientAsynchronous studentClient, CourseClient courseClient, EnrollmentRepository enrollmentRepository) {
        this.studentClient = studentClient;
        this.courseClient = courseClient;
        this.enrollmentRepository = enrollmentRepository;
    }
    @Override
    public Mono<EnrollmentResponseModel> addEnrollment(Mono<EnrollmentRequestModel> enrollmentRequestModel) {
        return enrollmentRequestModel
                .map(RequestContext::new)
                .flatMap(this::studentRequestResponse)
                .flatMap(this::courseRequestResponse)
                .map(EntityModelUtil::toEnrollmentEntity)
                .map(enrollmentRepository::save)
                .flatMap(entity -> entity)
                .map(EntityModelUtil::toEnrollmentResponseModel);
    }

    @Override
    public Flux<EnrollmentResponseModel> getAllEnrollments() {
        return enrollmentRepository.findAll()
                .map(EntityModelUtil::toEnrollmentResponseModel);
    }

    @Override
    public Mono<EnrollmentResponseModel> getEnrollmentByEnrollmentId(String enrollmentId) {
        return enrollmentRepository.findEnrollmentByEnrollmentId(enrollmentId)
                .switchIfEmpty(Mono.error(new NotFoundException("Enrollment id not found: " + enrollmentId)))
                .doOnNext(i -> log.debug("The enrollment entity is: " + i.toString()))
                .map(EntityModelUtil::toEnrollmentResponseModel)
                .log();
    }

    @Override
    public Mono<EnrollmentResponseModel> deleteEnrollmentByEnrollmentId(String enrollmentId) {
        return enrollmentRepository.findEnrollmentByEnrollmentId(enrollmentId)
                .switchIfEmpty(Mono.defer(
                        () -> Mono.error(
                                new NotFoundException
                                        ("Enrollment ID not found: " + enrollmentId))))
                .flatMap(existingEnrollment -> enrollmentRepository.delete(existingEnrollment)
                        .then(Mono.just(existingEnrollment)))
                .map(EntityModelUtil::toEnrollmentResponseModel);
    }

@Override
public Mono<EnrollmentResponseModel> updateEnrollmentByEnrollmentId(Mono<EnrollmentRequestModel> enrollmentRequestModel, String enrollmentId) {
    return enrollmentRepository.findEnrollmentByEnrollmentId(enrollmentId)
            .switchIfEmpty(Mono.error(new NotFoundException("Enrollment ID not found: " + enrollmentId)))
            .flatMap(existingEnrollment -> enrollmentRequestModel
                    .flatMap(requestModel -> {
                        // Calling CourseClient to ensure the courseId exists
                        return courseClient.getCourseByCourseId(requestModel.getCourseId())
                                .switchIfEmpty(Mono.error(new NotFoundException("Course ID not found: " + requestModel.getCourseId())))
                                .thenReturn(requestModel);
                    })
                    .flatMap(requestModel -> {
                        // Updating the enrollment with the new data from the request model
                        existingEnrollment.setEnrollmentYear(requestModel.getEnrollmentYear());
                        existingEnrollment.setSemester(requestModel.getSemester());
                        existingEnrollment.setStudentId(requestModel.getStudentId());
                        existingEnrollment.setStudentFirstName(requestModel.getStudentFirstName());
                        existingEnrollment.setStudentLastName(requestModel.getStudentLastName());
                        existingEnrollment.setCourseId(requestModel.getCourseId());
                        existingEnrollment.setCourseNumber(requestModel.getCourseNumber());
                        existingEnrollment.setCourseName(requestModel.getCourseName());
                        return enrollmentRepository.save(existingEnrollment);
                    })
                    .map(EntityModelUtil::toEnrollmentResponseModel)
            );
}

    private Mono<RequestContext> studentRequestResponse(RequestContext rc) {
        return this.studentClient
                .getStudentByStudentId(rc.getEnrollmentRequestModel().getStudentId())
                .doOnNext(rc::setStudentResponseModel)
                .thenReturn(rc);
    }

    private Mono<RequestContext> courseRequestResponse(RequestContext rc) {
        return this.courseClient.getCourseByCourseId(rc.getEnrollmentRequestModel()
                .getCourseId())
                .doOnNext(rc::setCourseResponseModel)
                .thenReturn(rc);
    }
}
