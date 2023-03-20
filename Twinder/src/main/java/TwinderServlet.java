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
import java.sql.*;
import java.util.concurrent.TimeoutException;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

// notice the name =... , value = ... is not necessary with mapping defined in web.xml
@WebServlet()
public class TwinderServlet extends HttpServlet {
    private static final String RPC_QUEUE_NAME = "rpc_queue";
    private static final String LOCAL_QUEUE_URL = "localhost";
    private static final String AWS_QUEUE_URL = "54.200.144.171";
    private static GenericObjectPool<Channel> channelPool;
    private static final boolean DEBUG = false;
    private static java.sql.Connection DbConnection;
    private static Gson gson;

    /**
     * Init several stuffs:
     * - Gson object
     * - rabbitQueue connection factory
     * - channel pool for rabbitQueue connections
     * - connection to Twinder sql database
     * @throws ServletException
     */
    @Override
    public void init() throws ServletException {
        super.init();
        gson = new Gson();
        // comes with rabbitMQ different from channelFactory
        ConnectionFactory rabbitConnectionFactory = new ConnectionFactory();
        rabbitConnectionFactory.setHost(LOCAL_QUEUE_URL);
//        rabbitConnectionFactory.setHost(AWS_QUEUE_URL);
        rabbitConnectionFactory.setUsername("admin");
        rabbitConnectionFactory.setPassword("admin");
        Connection connection;

        try {
            connection = rabbitConnectionFactory.newConnection();
            channelPool = new GenericObjectPool<>(new ChannelFactory(connection));
        } catch (TimeoutException | IOException e) {
            throw new ServletException(e);
        }

        try {
            String DbUrl = "jdbc:mysql://localhost:3306/Twinder";
            String username = "root";
            String passWord = "";
            Class.forName("com.mysql.cj.jdbc.Driver");
            DbConnection = DriverManager.getConnection(DbUrl, username, passWord);
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("db connect failed.");
            throw new RuntimeException(e);
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

    /**
     * Check whether the post url is legal.
     * @param urlParts string of array, expect /swipe/{left_or_right}/ = [, swipe, left]
     * @param response HttpServletResponse object
     * @return boolean on whether the url is valid
     * @throws IOException
     */
    private boolean postUrlIsValid(String[] urlParts, HttpServletResponse response) throws IOException {
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
        if (urlParts.length < 3 || !urlParts[2].matches("-?\\d+(\\.\\d+)?")) {
            response.getWriter().write("Invalid get url. Reject.");
            return;
        }

        int userID = Integer.parseInt(urlParts[2]);
        if (urlParts[1].matches("matches") && urlParts[2].matches("-?\\d+(\\.\\d+)?")) {
            response.getWriter().write("doGet for match successfully received!");
            try {
                getMatchData(userID);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else if (urlParts[1].matches("stats") && urlParts[2].matches("-?\\d+(\\.\\d+)?")) {
            response.getWriter().write("doGet for stats successfully received!");
//            TwinderStatsData statsData = getStatsData(userID);
        } else {
            response.getWriter().write("Invalid get url. Reject.");
            return;
        }

        String dsa = "well";
    }

//    private TwinderMatchData getMatchData(int userID) throws SQLException {
    private void getMatchData(int userID) throws SQLException {
        Statement statement = DbConnection.createStatement();
        PreparedStatement preparedStatement = null;
        String insertQueryStatement = "SELECT * FROM MatchData WHERE userID = ?";
        preparedStatement = DbConnection.prepareStatement(insertQueryStatement);
        preparedStatement.setInt(1, userID);
        ResultSet result = statement.executeQuery(insertQueryStatement);
        while(result.next()){
            System.out.println(result.getInt(1) + "," + result.getString(2));
            System.out.println(result.getString(3) + "," + result.getInt(4));
        }
    }

//    private TwinderStatsData getStatsData(int userID) {
//    }

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
            insertSwipeEvent2Db(jBody);
        } catch (SQLException e) {
            System.out.println("insert swipeEvent to database failed.");
            throw new RuntimeException(e);
        }
        try {
            channelPool.returnObject(channel);
            if (DEBUG) {
                System.out.println(jBody);
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

    /**
     * Insert the swipe event data into the Twinder.SwipeEvent database
     * @param swipeBody [id, swiper, swipee, comment, left_or_right, create_at]
     * @throws SQLException
     */
    private void insertSwipeEvent2Db(SwipeBody swipeBody) throws SQLException {
        if (DEBUG) {
            System.out.println(swipeBody);
        }
        String insertQueryStatement = "INSERT INTO SwipeEvent (swiper, swipee, comment, left_or_right) " +
                "VALUES (?,?,?,?)";
        PreparedStatement preparedStatement = DbConnection.prepareStatement(insertQueryStatement);
        preparedStatement.setInt(1, swipeBody.getSwiper());
        preparedStatement.setInt(2, swipeBody.getSwipee());
        preparedStatement.setString(3, swipeBody.getComment());
        preparedStatement.setString(4, swipeBody.getLeftOrRight());
        preparedStatement.executeUpdate();

        if (DEBUG) {
            System.out.println("insert Swipe Event success.");
        }
    }
}
