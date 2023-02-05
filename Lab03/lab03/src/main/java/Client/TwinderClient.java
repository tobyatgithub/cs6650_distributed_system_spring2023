package Client;

import io.swagger.client.ApiClient;
import io.swagger.client.api.SwipeApi;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map.Entry;
import java.util.Set;

public class TwinderClient {
    private static final int NUM_OF_THREADS = 10;
    private static final int NUM_OF_TASKS = 1_000;
    private static final boolean PRINT = false;

    private static Hashtable<String, String[]> recordTable = new Hashtable<String, String[]>() {{
        put("startTime", new String[NUM_OF_THREADS]);
        put("responseType", new String[NUM_OF_THREADS]);
        put("latency", new String[NUM_OF_THREADS]);
        put("responseCode", new String[NUM_OF_THREADS]);
    }};


    static AtomicInteger failRequestCounter = new AtomicInteger(0);
    static private String url = "http://18.236.26.147:8080/lab03_war/TwinderAPI/";
    // (500k,200) = 197741 ms; (1k, 1) = 49514 ms
    // static private String url = "http://54.184.114.172:8080/JavaServlet_war/twinder";
    // nick's endpoint, 1-28-2023 working!
    private int iterationNum;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(NUM_OF_THREADS);

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_OF_TASKS; i++) {
            ApiClient myClient = new ApiClient();
            myClient.setBasePath(url);
            SwipeApi apiInstance = new SwipeApi(myClient);
            executorService.submit(new Task(i, apiInstance, recordTable, failRequestCounter, PRINT));
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
        System.out.println("Number of request made: " + NUM_OF_TASKS);
        System.out.println("Number of fail request: " + failRequestCounter.get());
//        System.out.println(recordTable);
        // Write record table values to a csv
        Set<Entry<String, String[]>> entrySet = recordTable.entrySet();
        StringBuilder sb = new StringBuilder();
        for (Entry<String, String[]> entry : entrySet) {
            sb.append(entry.getKey());
            sb.append(",");
            sb.append(Arrays.toString(entry.getValue()));
            sb.append("\n");
        }

        try (FileWriter writer = new FileWriter("hashtable.csv")) {
            writer.write(sb.toString());
            System.out.println("The contents of the hashtable have been written to hashtable.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
