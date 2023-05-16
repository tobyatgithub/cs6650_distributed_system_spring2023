import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;

public class Worker implements Runnable {
    private final Connection connection;
    private final String queueName;
    private final ConcurrentHashMap<Integer, ArrayList<Integer>> concurrentHashMap;
    private final int qos;
    private final boolean DEBUG;
    private final java.sql.Connection dbConnection;
    private final Gson gson;

    public Worker(Connection connection, String queueName, ConcurrentHashMap<Integer, ArrayList<Integer>> concurrentHashMap,
                  int qos, boolean DEBUG, java.sql.Connection dbConnection) {
        this.connection = connection;
        this.queueName = queueName;
        this.concurrentHashMap = concurrentHashMap;
        this.qos = qos;
        this.DEBUG = DEBUG;
        this.dbConnection = dbConnection;

        this.gson = new Gson();
    }

    public Worker(Connection connection, String queueName, ConcurrentHashMap<Integer, ArrayList<Integer>> concurrentHashMap,
                  boolean DEBUG, java.sql.Connection dbConnection) {
        this(connection, queueName, concurrentHashMap, 1, DEBUG, dbConnection);
    }

    @Override
    public void run() {
        try {
            // TODO: try init it in constructor
            Channel channel = connection.createChannel();
            channel.queueDeclare(this.queueName, false, false, false, null);
            channel.basicQos(this.qos);
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                if (DEBUG) {
                    System.out.println(" [x] Received message:'" + message + "'");
                }
                doWork(message);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };
            channel.basicConsume(this.queueName, false, deliverCallback, consumerTag -> {
            });
        } catch (Exception e) {
            System.out.println("FAIL in run: \n");
            System.out.println(e);
        }
    }

    /**
     * Do the actual work where we:
     * 1. grab the SwipeBody object from the queue
     * 2. update the database
     * @param message a json format SwipeBody string
     */
    private void doWork(String message) {
        if (DEBUG) {
            System.out.println("do work! with message:");
            System.out.println(message);
        }
        SwipeBody jBody = gson.fromJson(message, SwipeBody.class);

        // TODO: add info to the concurrentHashMap
        // TODO: update database

        if (DEBUG) {
            System.out.println("toby here:");
            System.out.println(jBody + "\n");
        }
    }
}
