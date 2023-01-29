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
    private final SwipeApi apiInstance;
    private int iterationNum;
    private Metrics metrics;

    public Task(int iterationNum, SwipeApi apiInstance, Metrics metrics) {
        this.iterationNum = iterationNum;
        this.metrics = metrics;
        this.apiInstance = apiInstance;
    }


    @Override
    public void run() {
        System.out.println("Running task... " + iterationNum);
        String leftOrRight = new Random().nextBoolean() ? "left" : "right";
        String randomSwiperId = String.valueOf(new Random().nextInt(5000 - 1) + 1);
        String randomSwipeeId = String.valueOf(new Random().nextInt(1000000 - 1) + 1);

        SwipeDetails body = new SwipeDetails(); // SwipeDetails | response details
        body.setSwiper(randomSwiperId);
        body.setSwipee(randomSwipeeId);
        body.setComment("It happens to be: " + leftOrRight);


        int count = 0;
        int maxCount = 5;
        while (true) {
            try {
                apiInstance.swipe(body, leftOrRight);
                System.out.println(body);
                return;
            } catch (ApiException e) {
                if (++count >= maxCount) {
//                    count = 0;
                    System.err.println("Exception when calling SwipeApi#swipe");
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
}
