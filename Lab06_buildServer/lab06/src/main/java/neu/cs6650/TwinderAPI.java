package neu.cs6650;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import io.swagger.client.JSON;
import io.swagger.client.model.SwipeDetails;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

@WebServlet(name = "TwinderAPI", value = "/TwinderAPI/*")
public class TwinderAPI extends HttpServlet {
    private static final String RPC_QUEUE_NAME = "rpc_queue";
    private static final String LOCALHOST = "localhost";
    private static ObjectPool<Channel> channelPool;

    private static Gson gson;

//    @Override
//    public void init() throws ServletException {
//        super.init();
//        gson = new Gson();
//        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost(LOCALHOST);
//        Connection connection;
//        try {
//            connection = factory.newConnection();
//            channelPool = new GenericObjectPool<>(new ChannelFactory(connection));
//        } catch (TimeoutException | IOException e) {
//            throw new ServletException(e);
//        }
//    }
    public static void main(String[] args) throws IOException, TimeoutException {
        gson = new Gson();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(LOCALHOST);
        Connection connection = factory.newConnection();
        channelPool = new GenericObjectPool<>(new ChannelFactory(connection));
        System.out.println(" [x] TwinderAPI initialized.");
    }

    private static class ChannelFactory extends BasePooledObjectFactory<Channel> {

        private final Connection connection;

        ChannelFactory(Connection connection) {
            this.connection = connection;
        }

        @Override
        public Channel create() throws Exception {
            return connection.createChannel();
        }

        @Override
        public PooledObject<Channel> wrap(Channel channel) {
            return new DefaultPooledObject<>(channel);
        }

        @Override
        public void destroyObject(PooledObject<Channel> p) throws Exception {
            Channel channel = p.getObject();
            if (channel.isOpen()) {
                channel.close();
            }
        }
    }

    private boolean urlIsValid(String urlPath, HttpServletResponse response) throws IOException {
        // expect: /swipe/{leftorright}/ = [, swipe, left]
        String[] urlParts = urlPath.split("/");
        if (urlPath.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("empty link");
            return false;
        }
        if ((urlParts.length != 3) ||
                (!urlParts[1].matches("swipe")) ||
                (urlParts[2].matches("left") || urlParts[2].matches("right"))) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("missing parameters!");
            return false;
        }
        return true;
    }

    private boolean jsonIsValid(StringBuilder sBuilder, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String line;
        try {
            while ((line = request.getReader().readLine()) != null) {
                sBuilder.append(line);
            }
            gson.fromJson(sBuilder.toString(), SwipeDetails.class);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Successfully received a swipe.");
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String urlPath = request.getPathInfo();
        if (!urlIsValid(urlPath, response)) {
            System.out.println("FAIL: didn't pass url validation.");
            return;
        }
        // validate json
        StringBuilder sBuilder = new StringBuilder();
        if (!jsonIsValid(sBuilder, request, response)) {
            System.out.println("FAIL: didn't pass json validation.");
            return;
        }

        Channel channel = null;
        try {
            channel = channelPool.borrowObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
//        channel.queuePurge(RPC_QUEUE_NAME);
        channel.basicQos(1);
        System.out.println(" [x] Awaiting RPC requests");
        String contentString = gson.fromJson(sBuilder.toString(), JSON.class).toString();
        channel.basicPublish("", RPC_QUEUE_NAME, null, contentString.getBytes());
        Channel finalChannel = channel;
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(delivery.getProperties().getCorrelationId())
                    .build();

            String resultString = "";
            try {
                String message = new String(delivery.getBody(), "UTF-8");
                System.out.println("message: " + message + ")");
            } catch (Exception e) {
                System.out.println("error: " + e);
            } finally {
                finalChannel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, resultString.getBytes("UTF-8"));
                finalChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };

        channel.basicConsume(RPC_QUEUE_NAME, false, deliverCallback, (consumerTag -> {
        }));
    }
}
