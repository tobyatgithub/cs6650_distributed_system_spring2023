import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Consumer {
  private final static int NUM_THREADS = 200;
  private static String QUEUE_URL = "44.224.245.65";
//  private static String QUEUE_URL = "localhost";
  private static String QUEUE_NAME1 = "swipe_queue2";
  private static int QOS = 50;
  // The key is user ID, and integer array is [num_likes, num_dislikes, IDs...] value
  // Notice `IDs` are the given user who is swiped on.
  private static ConcurrentHashMap<Integer, ArrayList<Integer>> concurrentHashMap = new ConcurrentHashMap<>();

  public static void main(String[] args) throws Exception{
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(QUEUE_URL);
    factory.setUsername("admin");
    factory.setPassword("admin");
    Connection connection = factory.newConnection();
    // pre allocate the thread pools
    ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);
    for (int i = 0; i < NUM_THREADS; i++) {
      threadPool.submit(new Worker(connection, QUEUE_NAME1, concurrentHashMap, QOS));
    }
    threadPool.shutdown();
  }
  public int getNumLikes(int id) {
    if (!concurrentHashMap.containsKey(id)) {
      return 0;
    }
    return concurrentHashMap.get(id).get(0);
  }

  public int getNumDislikes(int id) {
    if (!concurrentHashMap.containsKey(id)) {
      return 0;
    }
    return concurrentHashMap.get(id).get(1);
  }

  public ArrayList<Integer> getPotentialMatch(int id) {
    if (!concurrentHashMap.containsKey(id) || concurrentHashMap.get(id).size() < 3) {
      return new ArrayList<>();
    }
    return new ArrayList<>(concurrentHashMap.get(id).subList(0, 2));
  }
}
