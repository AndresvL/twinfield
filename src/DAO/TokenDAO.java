package DAO;

import java.sql.*;
import java.util.ArrayList;

import DBUtil.DBConnection;
import object.Token;

public class TokenDAO {
	private static ResultSet output;

	public static Token getToken(String token, String name) throws SQLException {
		Statement statement;
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
			} else {
				// Set invalid when softwareToken is found in database with
				// another softwareName
				t = new Token("invalid", "invalid", "invalid", "invalid", softwareToken, softwareName);
			}
			break;
		}
		statement.close();

		return t;
	}

	public static String getSoftwareToken(String softwareToken, String name) throws SQLException {
		Statement statement;
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

		return token;

	}

	public static ArrayList<Token> getSoftwareTokens() throws SQLException {
		Statement statement;
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

		return allTokens;

	}

	public static void replaceToken(Token t) throws SQLException {
		Statement statement;
		Connection con = DBConnection.createDatabaseConnection();
		statement = con.createStatement();
		statement.execute(
				"REPLACE INTO credentials (softwareToken, accessToken, accessSecret, consumerToken, consumerSecret, softwareName)"
						+ "VALUES ('" + t.getSoftwareToken() + "','" + t.getAccessToken() + "','" + t.getAccessSecret()
						+ "','" + t.getConsumerToken() + "','" + t.getConsumerSecret() + "','" + t.getSoftwareName()
						+ "')");
		statement.close();

	}

	public static void saveToken(Token t) throws SQLException {
		if (getSoftwareToken(t.getSoftwareToken(), t.getSoftwareName()) == null) {
			Statement statement;
			Connection con = DBConnection.createDatabaseConnection();
			statement = con.createStatement();
			statement.execute(
					"REPLACE INTO credentials (softwareToken, accessToken, accessSecret, consumerToken, consumerSecret, softwareName)"
							+ "VALUES ('" + t.getSoftwareToken() + "','" + t.getAccessToken() + "','"
							+ t.getAccessSecret() + "','" + t.getConsumerToken() + "','" + t.getConsumerSecret() + "','"
							+ t.getSoftwareName() + "')");
			statement.close();

		}
	}

	public static void saveModifiedDate(String date, String softwareToken) throws SQLException {
		Statement statement;
		Connection con = DBConnection.createDatabaseConnection();
		statement = con.createStatement();
		statement.execute(
				"UPDATE credentials SET modified = \"" + date + "\" WHERE softwareToken = \"" + softwareToken + "\"");
		statement.close();

	}

	public static String getModifiedDate(String softwareToken) throws SQLException {
		Statement statement;
		String date = null;
		Connection con = DBConnection.createDatabaseConnection();
		statement = con.createStatement();
		output = statement
				.executeQuery("SELECT modified FROM credentials WHERE softwareToken =\"" + softwareToken + "\"");
		while (output.next()) {
			date = output.getString("modified");
		}
		statement.close();

		return date;
	}

	public static void deleteToken(String softwareToken) throws SQLException {
		Statement statement;
		Connection con = DBConnection.createDatabaseConnection();
		statement = con.createStatement();
		statement.execute("DELETE FROM credentials WHERE softwareToken =\"" + softwareToken + "\"");
		statement.close();

	}
}
