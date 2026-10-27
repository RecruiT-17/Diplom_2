package api;

import api.model.User;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@DisplayName("Тесты создания пользователя")
public class CreateUserTest extends BaseTest {

    private String email;
    private String password;
    private String name;
    private String accessToken;

    @Before
    public void generateData() {
        email = generateEmail();
        password = generatePassword();
        name = generateName();
    }

    @After
    public void cleanUp() {
        deleteUser(accessToken);
    }

    @Test
    @DisplayName("Создание уникального пользователя")
    @Description("Проверка успешной регистрации нового пользователя с валидными данными")
    public void createUniqueUserSuccess() {
        User user = new User(email, password, name);
        Response response = client.register(user);

        accessToken = response.path("accessToken");

        response.then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("user.email", equalTo(email))
                .body("user.name", equalTo(name))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    @DisplayName("Создание уже зарегистрированного пользователя")
    @Description("Повторная регистрация с тем же email должна вернуть ошибку 403")
    public void createDuplicateUserReturnsError() {
        User user = new User(email, password, name);

        Response first = client.register(user);
        accessToken = first.path("accessToken");
        first.then().statusCode(SC_OK);

        client.register(user)
                .then()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("Создание пользователя без обязательного поля email")
    @Description("Отсутствие email должно вернуть ошибку 403")
    public void createUserWithoutEmailReturnsError() {
        User user = new User();
        user.setPassword(password);
        user.setName(name);

        client.register(user)
                .then()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без обязательного поля password")
    @Description("Отсутствие password должно вернуть ошибку 403")
    public void createUserWithoutPasswordReturnsError() {
        User user = new User();
        user.setEmail(email);
        user.setName(name);

        client.register(user)
                .then()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без обязательного поля name")
    @Description("Отсутствие name должно вернуть ошибку 403")
    public void createUserWithoutNameReturnsError() {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        client.register(user)
                .then()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }
}