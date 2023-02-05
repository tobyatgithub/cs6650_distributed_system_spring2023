package Client;

import io.swagger.client.ApiException;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.SwipeDetails;

import java.util.Hashtable;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Task implements Runnable {
    private boolean PRINT;
    private final SwipeApi apiInstance;
    private final Hashtable<String, String[]> recordTable;
    private int iterationNum;
    private AtomicInteger failRequestCounter;

    public Task(int iterationNum, SwipeApi apiInstance, Hashtable<String, String[]> recordTable, AtomicInteger failRequestCounter, boolean PRINT) {
        this.iterationNum = iterationNum;
        this.apiInstance = apiInstance;
        this.recordTable = recordTable;
        this.failRequestCounter = failRequestCounter;
        this.PRINT = PRINT;
    }


    @Override
    public void run() {
        long start = System.currentTimeMillis();
        long threadId = Thread.currentThread().getId();

        System.out.println("Thread ID = " + threadId);

        recordTable.get("startTime")[(int) threadId] += Long.toString(start) + ", ";
        recordTable.get("responseType")[(int) threadId] += "POST, ";
        if(PRINT) {
            System.out.println("Running task... " + iterationNum);
        }
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
                if (PRINT) {
                    System.out.println(body);
                }
                recordTable.get("responseCode")[(int) threadId] += "200, ";
                break;
            } catch (ApiException e) {
                if (++count >= maxCount) {
                    failRequestCounter.getAndIncrement();
                    System.err.println("Exception when calling SwipeApi#swipe");
                    e.printStackTrace();
                    recordTable.get("responseCode")[(int) threadId] += "404, ";
                    break;
                }
            }
        }
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        recordTable.get("latency")[(int) threadId] += timeElapsed + ", ";
    }
}
