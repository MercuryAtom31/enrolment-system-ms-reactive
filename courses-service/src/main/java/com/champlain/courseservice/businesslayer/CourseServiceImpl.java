package com.champlain.courseservice.businesslayer;

import com.champlain.courseservice.dataaccesslayer.Course;
import com.champlain.courseservice.dataaccesslayer.CourseRepository;
import com.champlain.courseservice.presentationlayer.CourseRequestModel;
import com.champlain.courseservice.presentationlayer.CourseResponseModel;
import com.champlain.courseservice.utils.EntityModelUtil;
import com.champlain.courseservice.utils.exceptions.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
/**
This class interacts with the CourseRepository (which handles database operations)
and uses utility methods for converting between entity models and
response/request models.
 */
@Service
@Slf4j
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    public CourseServiceImpl(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    /**
     * The method returns a Flux<CourseResponseModel> containing all the courses.
     */
    @Override
    public Flux<CourseResponseModel> getAllCourses() {
        return courseRepository.findAll()
                .map(EntityModelUtil::toCourseResponseModel);//Converts one by one.
    }

    @Override
    public Mono<CourseResponseModel> getCourseByCourseId(String courseId) {
        /**
         * This retrieves a single Course entity from the database as a Mono.
         */
        return courseRepository.findCourseByCourseId(courseId)
                .switchIfEmpty(Mono.error(new NotFoundException("Course id not found: "
                        + courseId)))//Handling Empty Result.
                .doOnNext(i -> log.debug("The course entity is: " + i.toString()))
                //Convert Entity to Response Model.
                .map(EntityModelUtil::toCourseResponseModel)
                .log();
    }

    /**
     * Why do we need the generateUUIDString?
     * ANS: to generate the CourseId (available to clients) for the DB?
     * What about the id for the DB, how does it get generated?
     * <p>
     * What about the DB's id?
     *
     */
    @Override
    public Mono<CourseResponseModel> addCourse(Mono<CourseRequestModel> courseRequestModel) {
        return courseRequestModel
                .map(EntityModelUtil::toCourseEntity)
                .doOnNext(e -> e.setCourseId(EntityModelUtil.generateUUIDString()))
                .flatMap(courseRepository::save)
                .map(EntityModelUtil::toCourseResponseModel);
    }

    @Override
    public Mono<CourseResponseModel> updateCourseByCourseId(Mono<CourseRequestModel> courseRequestModel, String courseId) {

        //Must find it in the DB and then implement it.
        return courseRepository.findCourseByCourseId(courseId)
                //If the course is not found.
                .switchIfEmpty(Mono.defer(() -> Mono.error(new NotFoundException("Course id not found: " + courseId))))
                .flatMap(s -> courseRequestModel
                        .map(EntityModelUtil::toCourseEntity)
                        .doOnNext(e -> e.setCourseId(s.getCourseId()))
                        .doOnNext(e -> e.setId(s.getId()))
                )
                .flatMap(courseRepository::save)
                .map(EntityModelUtil::toCourseResponseModel);

    }

    @Override
    public Mono<CourseResponseModel> deleteCourseByCourseId(String courseId) {
        return courseRepository.findCourseByCourseId(courseId)
                .switchIfEmpty(Mono.defer(
                        () -> Mono.error(
                                new NotFoundException
                                        ("Course id not found: " + courseId))))
                .flatMap(existingCourse -> courseRepository.delete(existingCourse)
                        .then(Mono.just(existingCourse)))
                .map(EntityModelUtil::toCourseResponseModel);
    }
}
/*
 * Summary
 *
 *   Entity Conversion:
 *
 *   The utility class EntityModelUtil is used extensively
 *   to convert between entity classes (which represent database records)
 *   and response/request models (which are used to interact with clients).
 *
 *   Error Handling:
 *
 *   The code uses switchIfEmpty to handle cases where a course is not found,
 *   throwing a NotFoundException in such cases.
 *   This ensures that the client receives meaningful error messages.
 *
 */