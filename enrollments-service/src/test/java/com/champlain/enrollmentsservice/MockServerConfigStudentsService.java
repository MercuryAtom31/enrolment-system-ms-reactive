package com.champlain.enrollmentsservice;

import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.MediaType;
import org.springframework.http.HttpStatus;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;


public class MockServerConfigStudentsService {

    private static final Integer MOCK_SERVER_PORT = 7002;

    public final static String NON_EXISTING_STUDENT_ID = "1c9cb111-f7b5-408d-b572-28a55881eb27";

    private final ClientAndServer clientAndServer;

    private final MockServerClient mockServerClient = new MockServerClient("localhost", MOCK_SERVER_PORT);

    public MockServerConfigStudentsService() {
        this.clientAndServer = ClientAndServer.startClientAndServer(MOCK_SERVER_PORT);
    }

    public void registerGetStudent1ByStudentIdEndpoint() {

        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/api/v1/students/" + "c3540a89-cb47-4c96-888e-ff96708db4d8")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json("{\"studentId\":\"c3540a89-cb47-4c96-888e-ff96708db4d8\",\"firstName\":\"Donna\",\"lastName\":\"Hornsby\",\"program\":\"History\",\"stuff\":\"stuff\"}", MediaType.APPLICATION_JSON))
                );
    }

    public void registerGetStudent_NonExisting_ByStudentIdEndpoint() {


        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/api/v1/students/" + NON_EXISTING_STUDENT_ID)
                )
                .respond(
                        response()
                                .withStatusCode(HttpStatus.NOT_FOUND.value())
                );
    }

    //MISSING METHODS:

    public void registerGetStudent2ByStudentIdEndpoint() {
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/api/v1/students/" + "1f538db7-320a-4415-bad4-e1d44518b1ff")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withBody(json("{\"studentId\":\"1f538db7-320a-4415-bad4-e1d44518b1ff\",\"firstName\":\"Willis\",\"lastName\":\"Faraday\",\"program\":\"Pure and Applied Sciences\",\"stuff\":\"stuff\"}", MediaType.APPLICATION_JSON))
                );
    }

    public void registerGetStudent_INVALID_ByStudentIdEndpoint() {
        mockServerClient
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/api/v1/students/invalid-id")
                )
                .respond(
                        response()
                                .withStatusCode(422)
                                .withBody("{\"message\":\"Invalid student ID\"}")
                );
    }


    public void stopServer() {
        if (clientAndServer != null)
            this.clientAndServer.stop();
    }
}