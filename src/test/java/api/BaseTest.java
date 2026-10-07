package api;

import api.model.User;
import io.restassured.response.Response;
import org.junit.Before;

import java.util.List;

import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_OK;

public class BaseTest {

    protected StellarBurgersClient client;

    @Before
    public void setUp() {
        client = new StellarBurgersClient();
    }

    protected String createUserAndGetToken(String email, String password, String name) {
        User user = new User(email, password, name);
        Response response = client.register(user);


        String accessToken = response.path("accessToken");

        response.then().statusCode(SC_OK);

        return accessToken;
    }

    protected void deleteUser(String accessToken) {
        if (accessToken != null) {
            client.deleteUser(accessToken)
                    .then()
                    .statusCode(SC_ACCEPTED);
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

    protected List<String> getIngredientIds() {
        Response response = client.getIngredients();

        String bunId = response.path("data.find { it.type == 'bun' }._id");
        String sauceId = response.path("data.find { it.type == 'sauce' }._id");
        String mainId = response.path("data.find { it.type == 'main' }._id");

        return java.util.Arrays.asList(bunId, sauceId, mainId);
    }
}