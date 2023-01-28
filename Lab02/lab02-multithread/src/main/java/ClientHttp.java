import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ClientHttp {
    private static final int NUM_OF_THREADS = 500;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(NUM_OF_THREADS);

    public static void main(String[] args) {
        int NUM_OF_CLIENTS = 900;
        // 10,500 == 1148 ms, 100,500 == 1249 ms, 200,500 = 1260 ms, 300,500 == 2190 ms
        // 400,500 == 2216 ms, 500,500 == 3231 ms, 600,500 = 3207ms, 900,500 == 5198 ms
        // We’re interested in the number of threads Tomcat makes available to service requests.
        // By default, it is 200. To change:
        //<Connector port="8080" protocol="HTTP/1.1" maxThreads="10"...
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
