package api;

import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
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

    private String buildUserBody() {
        return String.format(
                "{\"email\":\"%s\",\"password\":\"%s\",\"name\":\"%s\"}",
                email, password, name);
    }

    @Test
    @DisplayName("Создание уникального пользователя")
    @Description("Проверка успешной регистрации нового пользователя с валидными данными")
    public void createUniqueUserSuccess() {
        Response response = given()
                .spec(requestSpec)
                .body(buildUserBody())
                .when()
                .post("/api/auth/register");

        response.then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("user.email", equalTo(email))
                .body("user.name", equalTo(name))
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());

        accessToken = response.path("accessToken");
    }

    @Test
    @DisplayName("Создание уже зарегистрированного пользователя")
    @Description("Повторная регистрация с тем же email должна вернуть ошибку 403")
    public void createDuplicateUserReturnsError() {
        Response first = given()
                .spec(requestSpec)
                .body(buildUserBody())
                .when()
                .post("/api/auth/register");
        first.then().statusCode(200);
        accessToken = first.path("accessToken");

        given()
                .spec(requestSpec)
                .body(buildUserBody())
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @Test
    @DisplayName("Создание пользователя без обязательного поля email")
    @Description("Отсутствие email должно вернуть ошибку 403")
    public void createUserWithoutEmailReturnsError() {
        String body = String.format(
                "{\"password\":\"%s\",\"name\":\"%s\"}",
                password, name);

        given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без обязательного поля password")
    @Description("Отсутствие password должно вернуть ошибку 403")
    public void createUserWithoutPasswordReturnsError() {
        String body = String.format(
                "{\"email\":\"%s\",\"name\":\"%s\"}",
                email, name);

        given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без обязательного поля name")
    @Description("Отсутствие name должно вернуть ошибку 403")
    public void createUserWithoutNameReturnsError() {
        String body = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\"}",
                email, password);

        given()
                .spec(requestSpec)
                .body(body)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(403)
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }
}