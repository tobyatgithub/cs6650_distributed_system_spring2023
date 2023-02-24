package org.distributed;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import org.apache.commons.pool2.impl.GenericObjectPool;


@WebServlet(name = "Twinder", value = "/twinder/*")
public class Twinder extends HttpServlet {
  private ConnectionFactory factory;
  private Connection connection;
  private GenericObjectPool<Channel> pool;
//  private static String QUEUE_URL = "44.224.245.65";
  private static String QUEUE_URL = "localhost";
  private static String QUEUE_NAME1 = "swipe_queue1";
  private static String QUEUE_NAME2 = "swipe_queue2";
  private static String FALLOUT_EXCHANGE = "fanout_exchange";
  private static int NUM_THREADS = 200;

  @Override public void init() {
    factory = new ConnectionFactory();
    factory.setHost(QUEUE_URL);
    factory.setUsername("admin");
    factory.setPassword("admin");
    try {
      connection = factory.newConnection();
      // Create our pool
      pool = new GenericObjectPool<>(new ChannelFactory(connection));
    } catch (TimeoutException | IOException e) {
      System.out.println(e);
    }
    // pre allocate the channels also set fix number of channels
    pool.setMaxTotal(NUM_THREADS);
    try {
      for (int i = 0; i < NUM_THREADS; i++) {
        pool.addObject();
      }
    } catch (Exception e) {
      System.out.println("Unable to allocate " + NUM_THREADS + "channels.");
    }
  }
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
  }

  private boolean isUrlValid(String[] urlParts) {
    // urlPath = "/swipe/{leftorright}/"
    // urlParts = [, swipe, left(right)]
    if (urlParts.length != 3) {
      return false;
    }

    if (!urlParts[1].matches("swipe")) {
      return false;
    }

    return urlParts[2].matches("left") || urlParts[2].matches("right");
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    response.setContentType("text/plain");
    Gson gson = new Gson();
    String urlPath = request.getPathInfo();
    String[] urlParts = urlPath.split("/");
    if (urlPath == null || urlPath.isEmpty() || !isUrlValid(urlParts)) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("missing parameters");
      return;
    }
    StringBuilder sb = new StringBuilder();
    String line;
    try {
      while ((line = request.getReader().readLine()) != null) {
        sb.append(line);
      }
      String leftOrRight = urlParts[2];
      SwipeBody jBody = (SwipeBody) gson.fromJson(sb.toString(), SwipeBody.class);
      jBody.setLeftOrRight(leftOrRight);
      String message = jBody.toString();
      Channel channel = pool.borrowObject();
      // the third parameter should set false, otherwise other channel can't use same QUEUE_NAME
      channel.queueDeclare(QUEUE_NAME1, false, false, false, null);
      channel.queueBind(QUEUE_NAME1, FALLOUT_EXCHANGE, "");
      channel.queueDeclare(QUEUE_NAME2, false, false, false, null);
      channel.queueBind(QUEUE_NAME2, FALLOUT_EXCHANGE, "");
      channel.basicPublish(FALLOUT_EXCHANGE, "", null, message.getBytes(StandardCharsets.UTF_8));
//      System.out.println(" [x] Sent '" + message + "'");
      response.setStatus(HttpServletResponse.SC_CREATED);
      response.getWriter().write(jBody.toString() + "\nposted");
      pool.returnObject(channel);
    } catch (Exception ex) {
      System.out.println(ex);
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().write("Something went wrong");
    }
  }
}