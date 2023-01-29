package Client;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.SwipeDetails;

import java.util.Random;

public class SingleClient {

    static private String url = "http://54.184.114.172:8080/JavaServlet_war/twinder";

    public static void main(String[] args) {
        ApiClient myClient = new ApiClient();
        myClient.setBasePath(url);
        SwipeApi apiInstance = new SwipeApi(myClient);

        String leftOrRight = new Random().nextBoolean() ? "left" : "right";
        String randomSwiperId = String.valueOf(new Random().nextInt(5000 - 1) + 1);
        String randomSwipeeId = String.valueOf(new Random().nextInt(1000000 - 1) + 1);

        SwipeDetails body = new SwipeDetails(); // SwipeDetails | response details
        body.setSwiper(randomSwiperId);
        body.setSwipee(randomSwipeeId);
        body.setComment("It happens to be: " + leftOrRight);
        try {
            apiInstance.swipe(body, leftOrRight);
            System.out.println(body);
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

}
