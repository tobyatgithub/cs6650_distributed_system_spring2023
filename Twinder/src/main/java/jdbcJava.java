import java.sql.*;
public class jdbcJava {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        String url = "jdbc:mysql://localhost:3306/sample";
        String username = "root";
        String passWord = "";
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection(url, username, passWord);

        testInsertDate(connection, 10, "test", "test@toby.com", 99);

        Statement statement = connection.createStatement();
        String query = "select * from users;";
        ResultSet result = statement.executeQuery(query);
        while(result.next()){
            System.out.println(result.getInt(1) + "," + result.getString(2));
            System.out.println(result.getString(3) + "," + result.getInt(4));
        }
        statement.close();
        connection.close();
    }

    private static void testInsertDate(Connection connection, int id, String name, String email, int age) throws SQLException {
        System.out.println("OK. Let's test.");
        PreparedStatement preparedStatement = null;
        String insertQueryStatement = "INSERT INTO users (id, name, email, age) " +
                "VALUES (?,?,?,?)";
            preparedStatement = connection.prepareStatement(insertQueryStatement);
            preparedStatement.setInt(1, id);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, email);
            preparedStatement.setInt(4, age);
            preparedStatement.executeUpdate();
    }
}
