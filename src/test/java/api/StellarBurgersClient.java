package api;

import api.model.Order;
import api.model.User;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

public class StellarBurgersClient {

    public static final String BASE_URL = "https://stellarburgers.education-services.ru";

    private final RequestSpecification requestSpec;

    public StellarBurgersClient() {
        RestAssured.baseURI = BASE_URL;
        this.requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBaseUri(BASE_URL)
                .addFilter(new AllureRestAssured())
                .build();
    }

    public RequestSpecification spec() {
        return requestSpec;
    }

    public Response register(User user) {
        return given()
                .spec(requestSpec)
                .body(user)
                .when()
                .post("/api/auth/register");
    }

    public Response login(User user) {
        return given()
                .spec(requestSpec)
                .body(user)
                .when()
                .post("/api/auth/login");
    }

    public Response deleteUser(String accessToken) {
        return given()
                .spec(requestSpec)
                .header("Authorization", accessToken)
                .when()
                .delete("/api/auth/user");
    }

    public Response getIngredients() {
        return given()
                .spec(requestSpec)
                .when()
                .get("/api/ingredients");
    }

    public Response createOrder(Order order, String accessToken) {
        RequestSpecification spec = given().spec(requestSpec).body(order);
        if (accessToken != null) {
            spec = spec.header("Authorization", accessToken);
        }
        return spec.when().post("/api/orders");
    }

    public Response createOrderWithRawBody(String rawBody, String accessToken) {
        RequestSpecification spec = given().spec(requestSpec).body(rawBody);
        if (accessToken != null) {
            spec = spec.header("Authorization", accessToken);
        }
        return spec.when().post("/api/orders");
    }
}
