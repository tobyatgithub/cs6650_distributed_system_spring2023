import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.swagger.client.model.SwipeDetails;

import javax.servlet.ServletException;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

// notice the name =... , value = ... is not necessary with mapping defined in web.xml
@WebServlet()
public class TwinderServlet extends HttpServlet {
    private static final String RPC_QUEUE_NAME = "rpc_queue";
    private static final String LOCALHOST = "localhost";
    private static final String QUEUE_URL = "54.200.144.171";
    private static GenericObjectPool<Channel> channelPool;
    private static final boolean DEBUG = false;

    private static Gson gson;

    @Override
    public void init() throws ServletException {
        super.init();
        gson = new Gson();
        // comes with rabbitMQ different from channelFactory
        ConnectionFactory rabbitConnectionFactory = new ConnectionFactory();
        rabbitConnectionFactory.setHost(LOCALHOST);
//        rabbitConnectionFactory.setHost(QUEUE_URL);
        rabbitConnectionFactory.setUsername("admin");
        rabbitConnectionFactory.setPassword("admin");
        Connection connection;
        try {
            connection = rabbitConnectionFactory.newConnection();
            channelPool = new GenericObjectPool<>(new ChannelFactory(connection));
        } catch (TimeoutException | IOException e) {
            throw new ServletException(e);
        }
    }

    private static class ChannelFactory extends BasePooledObjectFactory<Channel> {
        private final Connection connection;
        public ChannelFactory(Connection connection) {
            this.connection = connection;
        }

        @Override
        public Channel create() throws Exception {
            Channel newChannel = connection.createChannel();
            newChannel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
            return newChannel;
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

    private boolean postUrlIsValid(String[] urlParts, HttpServletResponse response) throws IOException {
        // expect: /swipe/{leftorright}/ = [, swipe, left]
        if (urlParts.length == 0) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("empty link");
            return false;
        }
        if ((urlParts.length != 3) ||
                (!urlParts[1].matches("swipe")) ||
                (!urlParts[2].matches("left") && !urlParts[2].matches("right"))) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("missing parameters!");
            return false;
        }
        if (DEBUG) {
            System.out.println("url validation passed.");
        }
        return true;
    }

    /**
     * @param urlParts: expect /matches/userID = [, matches, userID]
     * @return boolean: whether the getURL is valid or not
     */
    private boolean getUrlIsValid(String[] urlParts) {
        if (urlParts.length < 3) return false;
        return urlParts[1].matches("matches") && urlParts[2].matches("-?\\d+(\\.\\d+)?");
    }

    private boolean jsonIsValid(StringBuilder sBuilder, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String line;
        try {
            while ((line = request.getReader().readLine()) != null) {
                sBuilder.append(line);
            }
            // check whether this fromJson can run
            gson.fromJson(sBuilder.toString(), SwipeDetails.class);
            if (DEBUG) {
                System.out.println("Show the json:");
                System.out.println(sBuilder);
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        String urlPath = request.getPathInfo();
        String[] urlParts = urlPath.split("/");
        if (!getUrlIsValid(urlParts)) {
            response.getWriter().write("Invalid get url. Reject.");
            return;
        }
        response.getWriter().write("doGet successfully received!");
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        String urlPath = request.getPathInfo();
        String[] urlParts = urlPath.split("/");
        if (!postUrlIsValid(urlParts, response)) {
            System.out.println("FAIL: didn't pass url validation.");
            return;
        }
        StringBuilder sBuilder = new StringBuilder();
        if (!jsonIsValid(sBuilder, request, response)) {
            System.out.println("FAIL: didn't pass json validation.");
            return;
        }

        // send response to client
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("Successfully received a swipe.");

        Channel channel = null;
        try {
            channel = channelPool.borrowObject();
            if (DEBUG) {
                System.out.println("channel borrowed from channel pool.");
            }
        } catch (Exception e) {
            if (DEBUG) {
                System.out.println("channel borrow Failed.");
            }
            throw new RuntimeException(e);
        }
        if (DEBUG) {
            System.out.println(" [x] Awaiting RPC requests");
        }

        SwipeBody jBody = gson.fromJson(sBuilder.toString(), SwipeBody.class);
        String leftOrRight = urlParts[2];
        jBody.setLeftOrRight(leftOrRight);
        String jsonMessage = jBody.toString(); // turn into json format
        if (DEBUG) {
            System.out.println("Gson finished, content string = " + jsonMessage);
        }
        channel.basicPublish("", RPC_QUEUE_NAME, null, jsonMessage.getBytes(StandardCharsets.UTF_8));
        try {
            channelPool.returnObject(channel);
            if (DEBUG) {
                System.out.println("Return channel success.");
            }
        } catch (Exception e) {
            if (DEBUG) {
                System.out.println("Return channel fail.");
            }
            throw new RuntimeException(e);
        }
        if (DEBUG) {
            System.out.println("Success.\n\n");
        }
    }
}
