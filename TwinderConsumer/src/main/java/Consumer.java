import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class Consumer {
    private static final int QOS = 1;
    private static final int NUM_THREADS = 1;
    private static final String LOCALHOST = "localhost";
    private static final String QUEUE_URL = "54.200.144.171";
    private static final String QUEUE_NAME = "rpc_queue";
    private static ConcurrentHashMap<Integer, ArrayList<Integer>> concurrentHashMap = new ConcurrentHashMap<>();
    private static final boolean PRINT = false;

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost(QUEUE_URL);
        factory.setHost(LOCALHOST);
        factory.setUsername("admin");
        factory.setPassword("admin");
        Connection connection = factory.newConnection();

        // pre allocate the thread pools
        ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);
        for (int i = 0; i < NUM_THREADS; i++) {
            threadPool.submit(new Worker(connection, QUEUE_NAME, concurrentHashMap, QOS, PRINT));
        }
        threadPool.shutdown();
    }
}
