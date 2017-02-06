package DBUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
	private static Connection con = null;
	private static String password, user, url, host, port, database;

	public static Connection createDatabaseConnection() throws SQLException {
		localCon();
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = (Connection) DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return con;
	}

	public static void localCon() {
		url = "jdbc:mysql://localhost/Twinfield";
		user = "root";
		password = "";
	}

	public static void onlineCon() {
		// String url = "jdbc:mysql://172.31.8.201:3306/mydatabase";
		host = System.getenv("MYSQL_SERVICE_HOST");
		port = System.getenv("MYSQL_SERVICE_PORT");
		user = System.getenv("MYSQL_USER");
		password = System.getenv("MYSQL_PASSWORD");
		database = System.getenv("MYSQL_DATABASE");
		url = "jdbc:mysql://" + host + ":" + port + "/" + database;
	}
}
