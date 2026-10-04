package api;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;

public class BaseTest {

    protected static final String BASE_URL = "https://stellarburgers.education-services.ru";
    protected static RequestSpecification requestSpec;

    @Before
    public void setUp() {
        RestAssured.baseURI = BASE_URL;

        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBaseUri(BASE_URL)
                .addFilter(new AllureRestAssured())
                .build();
    }

    protected String createUserAndGetToken(String email, String password, String name) {
        String body = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\",\"name\":\"%s\"}",
                email, password, name);

        return RestAssured.given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .extract()
                .path("accessToken");
    }

    protected void deleteUser(String accessToken) {
        if (accessToken != null) {
            RestAssured.given()
                    .spec(requestSpec)
                    .header("Authorization", accessToken)
                    .when()
                    .delete("/api/auth/user")
                    .then()
                    .statusCode(202);
        }
    }

    protected String generateEmail() {
        return "test_" + System.currentTimeMillis() + "_" + (int) (Math.random() * 10000) + "@yandex.ru";
    }

    protected String generatePassword() {
        return "pass_" + (int) (Math.random() * 100000);
    }

    protected String generateName() {
        return "User_" + (int) (Math.random() * 10000);
    }
}