import java.sql.*;

public class jdbcJava {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        String url = "jdbc:mysql://localhost:3306/sample";
        String username = "root";
        String passWord = "";
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection connection = DriverManager.getConnection(url, username, passWord);

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
}
