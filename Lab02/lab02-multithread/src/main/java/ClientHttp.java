import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHttp {
    private static final int NUM_OF_THREADS = 20;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(NUM_OF_THREADS);

    public static void main(String[] args) {
        int NUM_OF_CLIENTS = 10;
        long start = System.currentTimeMillis();
        for (int i = 0; i < NUM_OF_CLIENTS; i++) {
            executorService.submit(new Task());
        }
        executorService.shutdown();

        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;
        System.out.println("Time spent: " + timeElapsed + " milliseconds");
    }
}
