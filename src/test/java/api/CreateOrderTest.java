package api;

import api.model.Order;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

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

        List<String> ids = getIngredientIds();
        bunId = ids.get(0);
        sauceId = ids.get(1);
        fillingId = ids.get(2);
    }

    @After
    public void cleanUp() {
        deleteUser(accessToken);
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и ингредиентами")
    @Description("Авторизованный пользователь создаёт заказ с валидными ID ингредиентов")
    public void createOrderWithAuthAndIngredientsSuccess() {
        Order order = new Order(Arrays.asList(bunId, sauceId, fillingId));

        client.createOrder(order, accessToken)
                .then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("name", notNullValue())
                .body("order.number", notNullValue());
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    @Description("Запрос без токена должен вернуть 401 Unauthorized")
    public void createOrderWithoutAuthReturnsError() {
        Order order = new Order(Arrays.asList(bunId, sauceId));

        client.createOrder(order, null)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    @Description("Пустой массив ингредиентов должен вернуть ошибку 400")
    public void createOrderWithoutIngredientsReturnsError() {
        Order order = new Order(java.util.Collections.emptyList());

        client.createOrder(order, accessToken)
                .then()
                .statusCode(SC_BAD_REQUEST)
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиентов")
    @Description("Невалидные ID ингредиентов должны вернуть ошибку 500")
    public void createOrderWithInvalidIngredientHashReturnsError() {
        String rawBody = "{\"ingredients\":[\"invalid_hash_12345\",\"invalid_hash_67890\"]}";

        client.createOrderWithRawBody(rawBody, accessToken)
                .then()
                .statusCode(SC_INTERNAL_SERVER_ERROR);
    }
}