package DAO;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import DBUtil.DBConnection;
import object.Settings;
import object.rest.Address;
import object.rest.Employee;
import object.rest.HourType;
import object.rest.Material;
import object.rest.Project;
import object.rest.Relation;

public class ObjectDAO {
	public static void saveEmployees(ArrayList<Employee> emp, String token) {
		try {
			Connection con = DBConnection.createDatabaseConnection();
			Statement statement = con.createStatement();
			for (Employee e : emp) {
				statement.execute("REPLACE INTO employees (code, firstname, lastname, softwareToken)" + "VALUES ('"
						+ e.getCode() + "','" + e.getFirstName() + "','" + e.getLastName() + "','" + token + "')");

			}
			con.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void saveMaterials(ArrayList<Material> mat, String token) {
		try {
			Connection con = DBConnection.createDatabaseConnection();
			Statement statement = con.createStatement();
			for (Material m : mat) {
				statement.execute("REPLACE INTO materials (code, description, price, unit, softwareToken)" + "VALUES ('"
						+ m.getCode() + "','" + m.getDescription() + "','" + m.getPrice() + "','" + m.getUnit() + "','"
						+ token + "')");
			}
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void saveProjects(ArrayList<Project> projects, String token) {
		Connection con;
		try {
			con = DBConnection.createDatabaseConnection();
			Statement statement = con.createStatement();
			for (Project p : projects) {
				statement.execute(
						"REPLACE INTO projects (code, code_ext, debtor_number, status, name, description, progress, date_start, date_end, active, softwareToken)"
								+ "VALUES ('" + p.getCode() + "','" + p.getCode_ext() + "','" + p.getDebtor_number()
								+ "','" + p.getStatus() + "','" + p.getName() + "','" + p.getDescription() + "','"
								+ p.getProgress() + "','" + p.getDate_start() + "','" + p.getDate_end() + "','"
								+ p.getActive() + "','" + token + "')");
			}
			con.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void saveRelations(ArrayList<Relation> relations, String token) {
		try {
			Connection con = DBConnection.createDatabaseConnection();
			Statement statement = con.createStatement();
			for (Relation r : relations) {
				for (Address a : r.getAddressess()) {
					statement.execute(
							"REPLACE INTO relations (name, code, contact, phone_number, email, email_workorder, street, house_number, postal_code, city, remark, type, addressId, softwareToken)"
									+ "VALUES ('" + r.getName() + "','" + r.getDebtorNumber() + "','" + r.getContact()
									+ "','" + a.getPhoneNumber() + "','" + a.getEmail() + "','" + r.getEmailWorkorder()
									+ "','" + a.getStreet().replace("'", "''") + "','" + a.getHouseNumber() + "','"
									+ a.getPostalCode() + "','" + a.getCity() + "','" + a.getRemark() + "','"
									+ a.getType() + "','" + a.getAddressId() + "','" + token + "')");

				}
			}
			con.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void saveHourTypes(ArrayList<HourType> hourtypes, String token) {
		try {
			Connection con = DBConnection.createDatabaseConnection();
			Statement statement = con.createStatement();
			for (HourType h : hourtypes) {
				statement.execute(
						"REPLACE INTO hourtypes (code, name, cost_booking, sale_booking, sale_price, cost_price, active, softwareToken)"
								+ "VALUES ('" + h.getCode() + "','" + h.getName() + "','" + h.getCostBooking() + "','"
								+ h.getSaleBooking() + "" + "','" + h.getCostPrice() + "','" + h.getSalePrice() + "','"
								+ h.getActive() + "','" + token + "')");

			}
			con.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static Boolean checkSoftwareToken(String softwareToken, String columnName, String codeString,
			String addressCode) throws SQLException {
		Connection con = DBConnection.createDatabaseConnection();
		Statement statement = con.createStatement();
		boolean b = false;
		ResultSet output;
		if (columnName.equals("relations")) {
			int code = Integer.parseInt(addressCode);
			output = statement.executeQuery("SELECT * FROM " + columnName + " WHERE softwareToken =\"" + softwareToken
					+ "\" AND addressId=" + code + " AND code=\"" + codeString + "\"");

		} else {
			output = statement.executeQuery("SELECT * FROM " + columnName + " WHERE softwareToken =\"" + softwareToken
					+ "\" AND code=\"" + codeString + "\"");
		}

		if (output.next()) {
			b = true;
		}
		return b;

	}

	public static Address getAddressID(String softwareToken, String addressType, String codeString) {
		Address a = null;
		Connection con;
		try {
			con = DBConnection.createDatabaseConnection();
			Statement statement = con.createStatement();
			ResultSet output = statement.executeQuery("SELECT * FROM relations WHERE softwareToken =\"" + softwareToken
					+ "\" AND type=\"" + addressType + "\"AND code=\"" + codeString + "\"");
			System.out.println("SELECT * FROM relations WHERE softwareToken=\"" + softwareToken
					+ "\" AND type=\"" + addressType + "\" AND code=\"" + codeString + "\"");
			if (output.next()) {
				a = new Address();
				String addressId = output.getString("addressId");
				System.out.println("addressDB " + addressId);
				a.setAddressId(Integer.parseInt(addressId));
				con.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return a;

	}

	public static void saveSettings(Settings set, String token) {
		try {
			Connection con = DBConnection.createDatabaseConnection();
			Statement statement = con.createStatement();
			statement.execute(
					"REPLACE INTO settings (import_office, export_office, factuur_type, import_types, softwareToken)"
							+ "VALUES ('" + set.getImportOffice() + "','" + set.getExportOffice() + "','"
							+ set.getFactuurType() + "','" + set.getImportObjects() + "','" + token + "')");

			con.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static Settings getSettings(String softwareToken) {
		Settings set = null;
		Connection con;
		try {
			con = DBConnection.createDatabaseConnection();
			Statement statement = con.createStatement();
			ResultSet output = statement
					.executeQuery("SELECT * FROM settings WHERE softwareToken =\"" + softwareToken + "\"");
			if (output.next()) {
				String importOffice = output.getString("import_office");
				String exportOffice = output.getString("export_office");
				String factuurType = output.getString("factuur_type");
				String importTypes = output.getString("import_types");
				importTypes = importTypes.replace("]", "");
				importTypes = importTypes.replace("[", "");
				String[] strValues = importTypes.split(",\\s");
				ArrayList<String> allTypes = new ArrayList<String>(Arrays.asList(strValues));
				set = new Settings(importOffice, exportOffice, factuurType, allTypes);
				con.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return set;
	}

	public static ArrayList<Map<String, String>> getLogs(String softwareToken) {
		ArrayList<Map<String, String>> allLogs = null;
		Connection con;
		try {
			allLogs = new ArrayList<Map<String, String>>();
			con = DBConnection.createDatabaseConnection();
			Statement statement = con.createStatement();
			ResultSet output = statement
					.executeQuery("SELECT * FROM log WHERE softwareToken =\"" + softwareToken + "\"");
			while (output.next()) {
				String timestamp = output.getString("timestamp");
				String messageString = output.getString("message");
				Map<String, String> logMap = new HashMap<String, String>();
				logMap.put("timestamp", timestamp);
				logMap.put("message", messageString);
				allLogs.add(logMap);
			}
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Collections.reverse(allLogs);
		return allLogs;
	}

	public static void saveLog(String log, String token) {
		// sys date
		LocalDateTime a = LocalDateTime.now();
		ZoneId zone = ZoneId.of("Europe/Paris");
		ZonedDateTime za = ZonedDateTime.of(a, zone).plusHours(1);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
		String timestamp = za.format(formatter);

		// delete old logs
		deleteLog(token);
		try {
			Connection con = DBConnection.createDatabaseConnection();
			Statement statement = con.createStatement();
			statement.execute("REPLACE INTO log (message, timestamp, softwareToken)" + "VALUES ('" + log + "','"
					+ timestamp + "','" + token + "')");

			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void deleteLog(String token) {
		ArrayList<Map<String, String>> allLogs = getLogs(token);
		// sys date
		LocalDateTime a = LocalDateTime.now();
		ZoneId zone = ZoneId.of("Europe/Paris");
		ZonedDateTime za = ZonedDateTime.of(a, zone).plusHours(1);
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
		Date currentTime;
		try {
			currentTime = format.parse(za.format(formatter));
			Connection con = DBConnection.createDatabaseConnection();
			Statement statement = con.createStatement();
			for (Map<String, String> log : allLogs) {
				String timestamp = log.get("timestamp");
				Date oldTime = format.parse(timestamp);
				long difference = currentTime.getTime() - oldTime.getTime();
				// Miliseconds
				if (difference >= 3600000) {
					statement.execute("DELETE FROM log WHERE softwareToken =\"" + token + "\" AND timestamp =\""
							+ timestamp + "\"");
				}
			}
			con.close();
		} catch (ParseException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
