package Client;

import io.swagger.client.ApiClient;
import io.swagger.client.api.SwipeApi;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//TODO:
public class TwinderClient {
    private static final int NUM_OF_THREADS = 200;
//    static private String url = "http://54.184.114.172:8080/lab03_war_exploded/TwinderAPI/";
//    static private String url = "http://localhost:8080/lab03_war_exploded/TwinderAPI/";

//    static private String url = "http://54.184.114.172:8080/JavaServlet_war/twinder";
    // nick's endpoint, 1-28-2023 working!
    private static Metrics metricsTracker;
    private int iterationNum;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(NUM_OF_THREADS);
    public TwinderClient(Metrics metricsTracker, int iterationNum, List<Metrics> metricsList) {
//        this.metricsList = metricsList;
        this.iterationNum = iterationNum;
        this.metricsTracker = metricsTracker;
    }

    public static Metrics getMetricsTracker() {
        return metricsTracker;
    }

    public static void main(String[] args) {
        System.out.println("hello.");
        int NUM_OF_CLIENTS = 500_000;
        long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_OF_CLIENTS; i++) {
            ApiClient myClient = new ApiClient();
            myClient.setBasePath(url);
            SwipeApi apiInstance = new SwipeApi(myClient);
            executorService.submit(new Task(i, apiInstance, metricsTracker));
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.println("Time spent: " + timeElapsed + " milliseconds");
        System.out.println("Request made: ");
        System.out.println(metricsTracker);
    }
}
