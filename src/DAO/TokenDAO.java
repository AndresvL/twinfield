package DAO;

import java.sql.*;
import java.util.ArrayList;

import DBUtil.DBConnection;
import object.Token;

public class TokenDAO {
	private static Statement statement;
	private static ResultSet output;

	public static Token getToken(String token, String name) throws SQLException {
		Token t = null;
		Connection con = DBConnection.createDatabaseConnection();
		statement = con.createStatement();
		output = statement.executeQuery("SELECT * FROM credentials WHERE softwareToken = \"" + token + "\"");
		while (output.next()) {
			String accessToken = output.getString("accessToken");
			String accessSecret = output.getString("accessSecret");
			String consumerToken = output.getString("consumerToken");
			String consumerSecret = output.getString("consumerSecret");
			String softwareToken = output.getString("softwareToken");
			String softwareName = output.getString("softwareName");
			if (softwareName.equals(name)) {
				t = new Token(consumerToken, consumerSecret, accessToken, accessSecret, softwareToken, softwareName);
			}
			break;
		}
		statement.close();
		con.close();
		return t;
	}

	public static String getSoftwareToken(String softwareToken, String name) throws SQLException {
		String token = null;
		Connection con = DBConnection.createDatabaseConnection();
		statement = con.createStatement();
		output = statement.executeQuery("SELECT * FROM credentials WHERE softwareToken =\"" + softwareToken + "\"");
		while (output.next()) {
			String softwareName = output.getString("softwareName");
			if (softwareName.equals(name)) {
				token = output.getString("softwareToken");
			}
		}
		statement.close();
		con.close();
		return token;

	}

	public static ArrayList<Token> getSoftwareTokens() throws SQLException {
		Token token = null;
		ArrayList<Token> allTokens = new ArrayList<Token>();
		Connection con = DBConnection.createDatabaseConnection();
		statement = con.createStatement();
		output = statement.executeQuery("SELECT * FROM credentials");
		while (output.next()) {
			String softwareToken = output.getString("softwareToken");
			String accessToken = output.getString("accessToken");
			String accessSecret = output.getString("accessSecret");
			String consumerToken = output.getString("consumerToken");
			String consumerSecret = output.getString("consumerSecret");
			String softwareName = output.getString("softwareName");
			token = new Token(consumerToken, consumerSecret, accessToken, accessSecret, softwareToken, softwareName);
			allTokens.add(token);
		}
		statement.close();
		con.close();
		return allTokens;

	}

	public static void saveToken(Token t) throws SQLException {
		if (getSoftwareToken(t.getSoftwareToken(), t.getSoftwareName()) == null) {
			Connection con = DBConnection.createDatabaseConnection();
			statement = con.createStatement();
			statement.execute(
					"REPLACE INTO credentials (softwareToken, accessToken, accessSecret, consumerToken, consumerSecret, softwareName)"
							+ "VALUES ('" + t.getSoftwareToken() + "','" + t.getAccessToken() + "','"
							+ t.getAccessSecret() + "','" + t.getConsumerToken() + "','" + t.getConsumerSecret() + "','"
							+ t.getSoftwareName() + "')");
			statement.close();
			con.close();
		}
	}

	public static void saveModifiedDate(String date, String softwareToken) throws SQLException {
		Connection con = DBConnection.createDatabaseConnection();
		statement = con.createStatement();
		statement.execute(
				"UPDATE credentials SET modified = \"" + date + "\" WHERE softwareToken = \"" + softwareToken + "\"");
		statement.close();
		con.close();
	}

	public static String getModifiedDate(String softwareToken) throws SQLException {
		String date = null;
		Connection con = DBConnection.createDatabaseConnection();
		statement = con.createStatement();
		output = statement
				.executeQuery("SELECT modified FROM credentials WHERE softwareToken =\"" + softwareToken + "\"");
		while (output.next()) {
			date = output.getString("modified");
		}
		statement.close();
		con.close();
		return date;
	}

	public static void deleteToken(String softwareToken) throws SQLException {
		Connection con = DBConnection.createDatabaseConnection();
		statement = con.createStatement();
		statement.execute("DELETE FROM credentials WHERE softwareToken =\"" + softwareToken + "\"");
		statement.close();
		con.close();
	}
}
