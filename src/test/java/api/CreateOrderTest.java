package api;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("Тесты создания заказа")
public class CreateOrderTest extends BaseTest {

    private String email;
    private String password;
    private String name;
    private String accessToken;

    private String bunId;
    private String sauceId;
    private String fillingId;

    @Before
    public void setUpOrderTest() {
        email = generateEmail();
        password = generatePassword();
        name = generateName();
        accessToken = createUserAndGetToken(email, password, name);

        Response ingredientsResponse = given()
                .spec(requestSpec)
                .get("/api/ingredients")
                .then()
                .statusCode(200)
                .extract()
                .response();

        List<String> allIds = ingredientsResponse.path("data._id");
        List<String> types = ingredientsResponse.path("data.type");

        for (int i = 0; i < types.size(); i++) {
            String type = types.get(i);
            String id = allIds.get(i);

            if ("bun".equals(type) && bunId == null) {
                bunId = id;
            } else if ("sauce".equals(type) && sauceId == null) {
                sauceId = id;
            } else if ("main".equals(type) && fillingId == null) {
                fillingId = id;
            }
        }
    }

    @After
    public void cleanUp() {
        deleteUser(accessToken);
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и ингредиентами")
    @Description("Авторизованный пользователь создаёт заказ с валидными ID ингредиентов")
    public void createOrderWithAuthAndIngredientsSuccess() {
        String body = String.format(
                "{\"ingredients\":[\"%s\",\"%s\",\"%s\"]}",
                bunId, sauceId, fillingId);

        given()
                .spec(requestSpec)
                .header("Authorization", accessToken)
                .body(body)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("name", notNullValue())
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    @Description("По документации запрос без токена должен вернуть 401 Unauthorized")
    public void createOrderWithoutAuthReturnsError() {
        String body = String.format(
                "{\"ingredients\":[\"%s\",\"%s\"]}",
                bunId, sauceId);

        given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    @Description("Пустой массив ингредиентов должен вернуть ошибку 400")
    public void createOrderWithoutIngredientsReturnsError() {
        given()
                .spec(requestSpec)
                .header("Authorization", accessToken)
                .body("{\"ingredients\":[]}")
                .when()
                .post("/api/orders")
                .then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиентов")
    @Description("Невалидные ID ингредиентов должны вернуть ошибку 500")
    public void createOrderWithInvalidIngredientHashReturnsError() {
        String body = "{\"ingredients\":[\"invalid_hash_12345\",\"invalid_hash_67890\"]}";

        given()
                .spec(requestSpec)
                .header("Authorization", accessToken)
                .body(body)
                .when()
                .post("/api/orders")
                .then()
                .statusCode(500)
                .body(containsString("Internal Server Error"));
    }
}