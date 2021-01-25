package es.urjc.code.daw.library.rest;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static io.restassured.path.json.JsonPath.from;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;

import io.restassured.RestAssured;
import io.restassured.response.Response;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookRestControllerTest {
    @LocalServerPort
    int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.baseURI = "https://localhost:" + port;
    }

    @Test
    @DisplayName("Given a not logged user, When requesting all books, Then is ok")
    public void getAllBooks() {

        when()
            .get("/api/books/")
        .then()
            .statusCode(200)
            .body("size()", equalTo(5))
            .body("[0].title", equalTo("SUEÑOS DE ACERO Y NEON"))
            .body("[1].title", equalTo("LA VIDA SECRETA DE LA MENTE"))
            .body("[2].title", equalTo("CASI SIN QUERER"))
            .body("[3].title", equalTo("TERMINAMOS Y OTROS POEMAS SIN TERMINAR"))
            .body("[4].title", equalTo("LA LEGIÓN PERDIDA"));
    }

    @Test
    @DisplayName("Given a logger user, When user creates a book, Then is ok")
    public void loggedUserCreatesABook() {

        Response response =
            given()
                .auth()
                .basic("user", "pass")
                .contentType("application/json")
                .body("{\"title\":\"Super libro\",\"description\":\"Super resumen\" }")
            .when()
                .post("/api/books/").andReturn();

        int id = from(response.getBody().asString()).get("id");

        when()
            .get("/api/books/{id}", id).
        then()
            .statusCode(200)
            .body("title", equalTo("Super libro"));
    }

    @Test
    public void loggedUserDeletesABook() {

        int id = createBookToRemove();

        given()
            .auth()
            .basic("admin", "pass")
        .when()
            .delete("/api/books/{id}", id)
        .then()
            .statusCode(200);

        when()
            .get("/api/books/{id}", id)
        .then()
            .statusCode(404);
    }

    private int createBookToRemove() {
        Response response =
            given().
                auth().
                basic("user", "pass").
                contentType("application/json").
                body("{\"title\":\"Super libro\",\"description\":\"Super resumen\" }").
            when().
                post("/api/books/");

        return from(response.getBody().asString()).get("id");
    }
}
