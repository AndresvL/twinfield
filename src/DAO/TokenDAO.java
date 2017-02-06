package DAO;

import java.sql.*;
import java.util.ArrayList;

import DBUtil.DBConnection;
import object.Token;

public class TokenDAO {
	private static Statement statement;
	private static ResultSet output;

	public Token getAccessToken() throws SQLException {
		Token t = null;
		Connection con = DBConnection.createDatabaseConnection();
		statement = con.createStatement();
		output = statement.executeQuery("SELECT * FROM credentials");
		while (output.next()) {
			String accessToken = output.getString("accessToken");
			String accessSecret = output.getString("accessSecret");
			String consumerToken = output.getString("consumerToken");
			String consumerSecret = output.getString("consumerSecret");
			String softwareToken = output.getString("softwareToken");
			t = new Token(consumerToken, consumerSecret, accessToken, accessSecret, softwareToken);
			break;
		}
		con.close();
		return t;
	}

	public static String getSoftwareToken(String softwareToken) throws SQLException {
		String token = null;
		Connection con = DBConnection.createDatabaseConnection();
		statement = con.createStatement();
		output = statement.executeQuery("SELECT * FROM credentials WHERE softwareToken =\"" + softwareToken + "\"");
		while (output.next()) {
			token = output.getString("softwareToken");
		}
		return token;

	}
	
	public static ArrayList<Token> getSoftwareTokens() throws SQLException {
		Token token = null;
		ArrayList<Token> allTokens = new ArrayList<Token>();;
		Connection con = DBConnection.createDatabaseConnection();
		statement = con.createStatement();
		output = statement.executeQuery("SELECT * FROM credentials");
		while (output.next()) {
			String softwareToken = output.getString("softwareToken");
			String accessToken = output.getString("accessToken");
			String accessSecret = output.getString("accessSecret");
			String consumerToken = output.getString("consumerToken");
			String consumerSecret = output.getString("consumerSecret");
			token = new Token(consumerToken, consumerSecret, accessToken, accessSecret, softwareToken);
			allTokens.add(token);
		}
		return allTokens;

	}
	public void saveAccessToken(Token t) throws SQLException {
		if (getSoftwareToken(t.getSoftwareToken()) == null) {
			Connection con = DBConnection.createDatabaseConnection();
			statement = con.createStatement();
			statement.execute(
					"INSERT INTO credentials (softwareToken, accessToken, accessSecret, consumerToken, consumerSecret)"
							+ "VALUES ('" + t.getSoftwareToken() + "','" + t.getAccessToken() + "','"
							+ t.getAccessSecret() + "','" + t.getConsumerToken() + "','" + t.getConsumerSecret()
							+ "')");
			con.close();
		}
	}

	public static void deleteToken(String softwareToken) throws SQLException {
		Connection con = DBConnection.createDatabaseConnection();
		statement = con.createStatement();
		statement.execute("DELETE FROM credentials WHERE softwareToken =\"" + softwareToken + "\"");
		con.close();
	}
}
