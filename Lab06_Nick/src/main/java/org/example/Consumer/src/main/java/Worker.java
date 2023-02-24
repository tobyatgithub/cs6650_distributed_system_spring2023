import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;

public class Worker implements Runnable{
  private Connection connection;
  private int qos;
  private String queueName;
  private ConcurrentHashMap<Integer, ArrayList<Integer>> concurrentHashMap;

  public Worker(Connection connection, String queueName, ConcurrentHashMap<Integer, ArrayList<Integer>> concurrentHashMap, int qos) {
    this.concurrentHashMap = concurrentHashMap;
    this.connection = connection;
    this.queueName = queueName;
    this.qos = qos;
  }

  // Default construct set qos as 1
  public Worker(Connection connection, String queueName, ConcurrentHashMap<Integer, ArrayList<Integer>> concurrentHashMap) {
    this(connection, queueName, concurrentHashMap ,1);
  }

  @Override
  public void run() {
    try {
      Channel channel = connection.createChannel();
      channel.queueDeclare(this.queueName, false, false, false, null);
      channel.basicQos(this.qos);
      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
//        System.out.println(" [x] Received '" + message + "'");
        doWork(message);
//        System.out.println(" [x] Done");
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
      };
      boolean autoAck = false;
      channel.basicConsume(this.queueName, autoAck, deliverCallback, consumerTag->{});
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  private void doWork(String message) {
    Gson gson = new Gson();
    SwipeBody jBody = (SwipeBody) gson.fromJson(message, SwipeBody.class);
    int swiper = jBody.getSwiper();
    int swipee = jBody.getSwipee();
    // If user not in our map, we initialize the value as [0, 0]
    concurrentHashMap.putIfAbsent(swiper, new ArrayList<>(Arrays.asList(0, 0)));
    concurrentHashMap.putIfAbsent(swipee, new ArrayList<>(Arrays.asList(0, 0)));
    if (jBody.getLeftOrRight().matches("left")) {
      concurrentHashMap.compute(swiper, (key, value) -> {
        value.set(1, value.get(1)+1);
        return value;
      });
    } else {
      // Use compute to ensure atomic operations to read and modify the value accordingly
      concurrentHashMap.compute(swiper, (key, value) -> {
        value.set(0, value.get(0)+1);
        return value;
      });
      concurrentHashMap.compute(swipee, (key, value) -> {
        value.add(swiper);
        return value;
      });
    }
  }
}
