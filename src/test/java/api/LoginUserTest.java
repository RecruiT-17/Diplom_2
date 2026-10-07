package api;

import api.model.User;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
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
        User user = new User(email, password);

        client.login(user)
                .then()
                .statusCode(SC_OK)
                .body("success", equalTo(true))
                .body("user.email", equalTo(email))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
    }

    @Test
    @DisplayName("Вход с неверным логином")
    @Description("Несуществующий email должен вернуть ошибку 401")
    public void loginWithWrongEmailReturnsError() {
        User user = new User("wrong_" + email, password);

        client.login(user)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    @Test
    @DisplayName("Вход с неверным паролем")
    @Description("Неверный password должен вернуть ошибку 401")
    public void loginWithWrongPasswordReturnsError() {
        User user = new User(email, "wrong_password_" + (int) (Math.random() * 10000));

        client.login(user)
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }
}