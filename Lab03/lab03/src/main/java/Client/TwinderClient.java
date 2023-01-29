package Client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TwinderClient {
    private static final int NUM_OF_THREADS = 10;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(NUM_OF_THREADS);

    public static void main(String[] args) {
        int NUM_OF_CLIENTS = 50;
        long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_OF_CLIENTS; i++) {
            executorService.submit(new Task());
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
    }
}
