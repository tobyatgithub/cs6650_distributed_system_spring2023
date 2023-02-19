package neu.cs6650.Client;

import com.sun.tools.javac.Main;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.SwipeDetails;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Task implements Runnable {
    private final FileWriter writer;
    private final boolean PRINT;
    private final SwipeApi apiInstance;
    private final int iterationNum;
    private final AtomicInteger failRequestCounter;

    public Task(int iterationNum, SwipeApi apiInstance, AtomicInteger failRequestCounter, boolean PRINT, FileWriter writer) throws IOException {
        this.iterationNum = iterationNum;
        this.apiInstance = apiInstance;
        this.failRequestCounter = failRequestCounter;
        this.PRINT = PRINT;
        this.writer = writer;
    }


    @Override
    public void run() {
        long start = System.currentTimeMillis();
        if(PRINT) {
            System.out.println("Running task... " + iterationNum);
        }
        String leftOrRight = new Random().nextBoolean() ? "left" : "right";
        String randomSwiperId = String.valueOf(new Random().nextInt(5000 - 1) + 1);
        String randomSwipeeId = String.valueOf(new Random().nextInt(1000000 - 1) + 1);

        SwipeDetails body = new SwipeDetails(); // SwipeDetails | response details
        body.setSwiper(randomSwiperId); // swiper
        body.setSwipee(randomSwipeeId); // swipee
        body.setComment("It happens to be: " + leftOrRight); // comment


        int count = 0;
        int maxCount = 5;
        String statusCode = "404";
        while (true) { // maybe retry, or for loop
            try {
                ApiResponse<Void> response = apiInstance.swipeWithHttpInfo(body, leftOrRight);
                statusCode = String.valueOf(response.getStatusCode());
                if (PRINT) {
                    System.out.println(body);
                }
                break;
            } catch (ApiException e) {
                if (++count >= maxCount) {
                    failRequestCounter.getAndIncrement();
                    System.err.println("Exception when calling SwipeApi#swipe");
                    e.printStackTrace();
                    break;
                }
            }
        }
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;

        synchronized (Main.class) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String readableStart = dateFormat.format(start);
                writer.append( readableStart);
                writer.append(", ");
                writer.append("POST, ");
                writer.append(String.valueOf(timeElapsed));
                writer.append(", ");
                writer.append(statusCode + ", ");
                writer.append("\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
