package api;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@DisplayName("Тесты логина пользователя")
public class LoginUserTest extends BaseTest {

    private String email;
    private String password;
    private String name;
    private String accessToken;

    @Before
    public void createUser() {
        email = generateEmail();
        password = generatePassword();
        name = generateName();
        accessToken = createUserAndGetToken(email, password, name);
    }

    @After
    public void cleanUp() {
        deleteUser(accessToken);
    }

    @Test
    @DisplayName("Вход под существующим пользователем")
    @Description("Успешный логин с валидными email и password")
    public void loginWithValidCredentialsSuccess() {
        String body = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\"}",
                email, password);

        given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(email))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    @DisplayName("Вход с неверным логином")
    @Description("Несуществующий email должен вернуть ошибку 401")
    public void loginWithWrongEmailReturnsError() {
        String body = String.format(
                "{\"email\":\"wrong_%s\",\"password\":\"%s\"}",
                email, password);

        given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Вход с неверным паролем")
    @Description("Неверный password должен вернуть ошибку 401")
    public void loginWithWrongPasswordReturnsError() {
        String body = String.format(
                "{\"email\":\"%s\",\"password\":\"wrong_password_%d\"}",
                email, (int) (Math.random() * 10000));

        given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }
}