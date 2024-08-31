package com.champlain.enrollmentsservice.businesslayer.enrollments;

import com.champlain.enrollmentsservice.dataaccesslayer.Enrollment;
import com.champlain.enrollmentsservice.domainclientlayer.Courses.CourseResponseModel;
import com.champlain.enrollmentsservice.domainclientlayer.Students.StudentResponseModel;
import com.champlain.enrollmentsservice.presentationlayer.enrollments.EnrollmentRequestModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
Understanding the Role of RequestContext
 <p/>
The RequestContext class serves as a data container that holds all the information
 needed during the process of creating a new enrollment.
 <p/>
 This includes:
 <p/>
EnrollmentRequestModel enrollmentRequestModel:
 <p/>
 This holds the data that the client sends when requesting to create a new enrollment.
 <p/>
StudentResponseModel studentResponseModel:
 <p/>
 This holds the data retrieved from
 the StudentClientAsynchronous when the service fetches details about
 the student involved in the enrollment.
 <p/>
CourseResponseModel courseResponseModel:
 <p/>
 This holds the data retrieved from the CourseClient when the service fetches
 details about the course involved in the enrollment.
 <p/>
Enrollment enrollment:
 <p/>
 This will hold the enrollment entity that is eventually saved to the database.
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestContext {

    private EnrollmentRequestModel enrollmentRequestModel;
    private Enrollment enrollment;

    private StudentResponseModel studentResponseModel;

    private CourseResponseModel courseResponseModel;
    public RequestContext(EnrollmentRequestModel enrollmentRequestModel) {
        this.enrollmentRequestModel = enrollmentRequestModel;
    }
}



/*
Why do we have the Request Model for the enrollment but the Response Models from
the student and course?

What does the enrollment entity do here?

*Why do we ONLY initialize the enrollmentRequestModel inside the constructor
 and not the others too?

 * What is an example of what it will hold?
 The RequestContext is created using the EnrollmentRequestModel.
 This context will hold all relevant information as the process progresses.
 */