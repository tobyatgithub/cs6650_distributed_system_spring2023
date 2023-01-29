package Client;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.SwipeDetails;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.IOException;
import java.util.Random;

public class Task implements Runnable {
    private String url = "http://localhost:8080/lab03_war_exploded/TwinderAPI/swipe/";
    @Override
    public void run() {
        SwipeApi apiInstance = new SwipeApi();
        SwipeDetails body = new SwipeDetails(); // SwipeDetails | response details
        String leftOrRight = new Random().nextBoolean() ? "left" : "right";
        String randomSwiperId = String.valueOf(new Random().nextInt(5000-1) + 1);
        String randomSwipeeId = String.valueOf(new Random().nextInt(1000000-1) + 1);
        body.setSwiper(randomSwiperId);
        body.setSwipee(randomSwipeeId);
        body.setComment("It happens to be: " + leftOrRight);
        try {
            apiInstance.swipe(body, leftOrRight);
//            System.out.println("hmmm");
            System.out.println(body);
        } catch (ApiException e) {
            System.err.println("Exception when calling SwipeApi#swipe");
            e.printStackTrace();
        }

    }
}
