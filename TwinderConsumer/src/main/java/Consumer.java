import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
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
        java.sql.Connection dbConnection = null;
        try {
            String DbUrl = "jdbc:mysql://localhost:3306/Twinder";
            String username = "root";
            String passWord = "";
            Class.forName("com.mysql.cj.jdbc.Driver");
            dbConnection = DriverManager.getConnection(DbUrl, username, passWord);
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("db connect failed.");
            throw new RuntimeException(e);
        }

        // pre allocate the thread pools
        ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);
        for (int i = 0; i < NUM_THREADS; i++) {
            threadPool.submit(new Worker(connection, QUEUE_NAME, concurrentHashMap, QOS, PRINT, dbConnection));
        }
        threadPool.shutdown();
    }
}
