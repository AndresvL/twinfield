package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import object.workorder.Address;
import object.workorder.Employee;
import object.workorder.HourType;
import object.workorder.Material;
import object.workorder.Project;
import object.workorder.Relation;

public class ObjectDAO {
	public static void saveEmployees(ArrayList<Employee> emp, String token) throws SQLException {
		PreparedStatement stmt = null;
		Connection con = null;
		try {
			con = DBConnection.createDatabaseConnection(true);
			for (Employee e : emp) {
				stmt = con.prepareStatement(
						"REPLACE INTO employees (code, firstname, lastname, softwareToken) values (?, ?, ?, ?)");
				stmt.setString(1, e.getCode());
				stmt.setString(2, e.getFirstName());
				stmt.setString(3, e.getLastName());
				stmt.setString(4, token);
				stmt.executeUpdate();
			}
			
			stmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveMaterials(ArrayList<Material> mat, String token) {
		PreparedStatement stmt = null;
		try {
			Connection con = DBConnection.createDatabaseConnection(true);
			for (Material m : mat) {
				String subCode = "null";
				if (m.getSubCode() != null) {
					if (!m.getSubCode().equals("")) {
						subCode = m.getSubCode();
					}
				}
				stmt = con.prepareStatement(
						"REPLACE INTO materials (code, subcode, description, price, unit, id, modified, softwareToken) values (?, ?, ?, ?, ?, ?, ?, ?)");
				stmt.setString(1, m.getCode());
				stmt.setString(2, subCode);
				stmt.setString(3, m.getDescription());
				stmt.setDouble(4, m.getPrice());
				stmt.setString(5, m.getUnit());
				stmt.setString(6, m.getId());
				stmt.setString(7, m.getModified());
				stmt.setString(8, token);
				stmt.executeUpdate();
			}
			
			stmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static Material getMaterials(String softwareToken, String subCode) throws SQLException {
		Material m = null;
		PreparedStatement stmt = null;
		Connection con = DBConnection.createDatabaseConnection(true);
		try {
			String selectSQL = "SELECT * FROM materials WHERE softwareToken = ? AND subCode = ?";
			stmt = con.prepareStatement(selectSQL);
			stmt.setString(1, softwareToken);
			stmt.setString(2, subCode);
			ResultSet output = stmt.executeQuery();
			// Check if subCode exists
			while (output.next()) {
				String code = output.getString("code");
				String description = output.getString("description");
				Double price = output.getDouble("price");
				String unit = output.getString("unit");
				String id = output.getString("id");
				m = new Material(code, subCode, unit, description, price, null, null, id);
			}
			
			// check if code exists
			if (m == null) {
				selectSQL = "SELECT * FROM materials WHERE softwareToken = ? AND code = ?";
				stmt = con.prepareStatement(selectSQL);
				stmt.setString(1, softwareToken);
				stmt.setString(2, subCode);
				output = stmt.executeQuery();
				while (output.next()) {
					String description = output.getString("description");
					Double price = output.getDouble("price");
					String unit = output.getString("unit");
					String id = output.getString("id");
					m = new Material(subCode, null, unit, description, price, null, null, id);
				}
			}
			output.close();
			stmt.close();
			
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return m;
	}
	
	public static void saveProjects(ArrayList<Project> projects, String token) {
		PreparedStatement stmt = null;
		try {
			Connection con = DBConnection.createDatabaseConnection(true);
			for (Project p : projects) {
				stmt = con.prepareStatement(
						"REPLACE INTO projects (code, code_ext, debtor_number, status, name, description, progress, date_start, date_end, active, softwareToken) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				stmt.setString(1, p.getCode());
				stmt.setString(2, p.getCodeExt());
				stmt.setString(3, p.getDebtorNumber());
				stmt.setString(4, p.getStatus());
				stmt.setString(5, p.getName());
				stmt.setString(6, p.getDescription());
				stmt.setInt(7, p.getProgress());
				stmt.setString(8, p.getDate_start());
				stmt.setString(9, p.getDate_end());
				stmt.setInt(10, p.getActive());
				stmt.setString(11, token);
				stmt.executeUpdate();
			}
			stmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static Project getProjectByCode(String softwareToken, String code) {
		Project p = null;
		PreparedStatement stmt = null;
		try {
			Connection con = DBConnection.createDatabaseConnection(true);
			String selectSQL = "SELECT * FROM projects WHERE softwareToken = ? AND code = ?";
			stmt = con.prepareStatement(selectSQL);
			stmt.setString(1, softwareToken);
			stmt.setString(2, code);
			ResultSet output = stmt.executeQuery();
			while (output.next()) {
				String id = output.getString("code_ext");
				String debtorNumber = output.getString("debtor_number");
				String status = output.getString("status");
				String name = output.getString("name");
				String description = output.getString("description");
				String dateStart = output.getString("date_start");
				String dateEnd = output.getString("date_end");
				p = new Project(code, id, debtorNumber, status, name, dateStart, dateEnd, description, 0, 1, null);
			}
			output.close();
			stmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return p;
	}
	
	public static Project getProjectById(String softwareToken, String id) {
		Project p = null;
		PreparedStatement stmt = null;
		try {
			Connection con = DBConnection.createDatabaseConnection(true);
			String selectSQL = "SELECT * FROM projects WHERE softwareToken = ? AND code_ext = ?";
			stmt = con.prepareStatement(selectSQL);
			stmt.setString(1, softwareToken);
			stmt.setString(2, id);
			ResultSet output = stmt.executeQuery();
			while (output.next()) {
				String code = output.getString("code");
				String debtorNumber = output.getString("debtor_number");
				String status = output.getString("status");
				String name = output.getString("name");
				String description = output.getString("description");
				String dateStart = output.getString("date_start");
				String dateEnd = output.getString("date_end");
				p = new Project(code, id, debtorNumber, status, name, dateStart, dateEnd, description, 0, 1, null);
			}
			output.close();
			stmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return p;
	}
	
	public static void saveRelations(ArrayList<Relation> relations, String token) {
		PreparedStatement stmt = null;
		try {
			Connection con = DBConnection.createDatabaseConnection(true);
			for (Relation r : relations) {
				for (Address a : r.getAddressess()) {
					stmt = con.prepareStatement(
							"REPLACE INTO relations (name, code, contact, phone_number, email, email_workorder, street, house_number, postal_code, city, remark, type, addressId, id, modified, softwareToken) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
					stmt.setString(1, r.getCompanyName());
					stmt.setString(2, r.getDebtorNumber());
					stmt.setString(3, a.getName());
					stmt.setString(4, a.getPhoneNumber());
					stmt.setString(5, a.getEmail());
					stmt.setString(6, r.getEmailWorkorder());
					stmt.setString(7, a.getStreet());
					stmt.setString(8, a.getHouseNumber());
					stmt.setString(9, a.getPostalCode());
					stmt.setString(10, a.getCity());
					stmt.setString(11, a.getRemark());
					stmt.setString(12, a.getType());
					stmt.setInt(13, a.getAddressId());
					stmt.setString(14, r.getId());
					stmt.setString(15, r.getModified());
					stmt.setString(16, token);
					
					stmt.executeUpdate();
				}
				stmt.close();
				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static Relation getRelation(String softwareToken, String debtorCode, String type) {
		Relation r = null;
		Address adr = null;
		ArrayList<Address> allAddresses = null;
		PreparedStatement stmt = null;
		String companyName = null, emailWorkorder = null, contact = null, modified = null;
		try {
			Connection con = DBConnection.createDatabaseConnection(true);
			String selectSQL = "SELECT * FROM relations WHERE softwareToken = ? AND code = ? AND type = ?";
			
			stmt = con.prepareStatement(selectSQL);
			stmt.setString(1, softwareToken);
			stmt.setString(2, debtorCode);
			stmt.setString(3, type);
			System.out.println("QUERY " + stmt.toString());
			ResultSet output = stmt.executeQuery();
			while (output.next()) {
				allAddresses = new ArrayList<Address>();
				companyName = output.getString("name");
				contact = output.getString("contact");
				emailWorkorder = output.getString("email_workorder");
				modified = output.getString("modified");
				// Address
				String email = output.getString("email");
				String street = output.getString("street");
				String houseNumber = output.getString("house_number");
				String postalCode = output.getString("postal_code");
				String city = output.getString("city");
				String phoneNumber = output.getString("phone_number");
				String remark = output.getString("remark");
				String id = output.getString("id");
				int addressId = output.getInt("addressId");
				adr = new Address(contact, phoneNumber, email, street, houseNumber, postalCode, city, remark, type,
						addressId);
				allAddresses.add(adr);
				r = new Relation(companyName, debtorCode, contact, emailWorkorder, allAddresses, modified, id);
			}
			output.close();
			stmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return r;
	}
	
	public static void saveHourTypes(ArrayList<HourType> hourtypes, String token) {
		PreparedStatement stmt = null;
		try {
			Connection con = DBConnection.createDatabaseConnection(true);
			for (HourType h : hourtypes) {
				stmt = con.prepareStatement(
						"REPLACE INTO hourtypes (code, name, cost_booking, sale_booking, sale_price, cost_price, active, id, modified, softwareToken) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				stmt.setString(1, h.getCode());
				stmt.setString(2, h.getName());
				stmt.setInt(3, h.getCostBooking());
				stmt.setInt(4, h.getSaleBooking());
				stmt.setDouble(5, h.getSalePrice());
				stmt.setDouble(6, h.getCostPrice());
				stmt.setInt(7, h.getActive());
				stmt.setString(8, h.getId());
				stmt.setString(9, h.getModified());
				stmt.setString(10, token);
				stmt.executeUpdate();
			}
			
			stmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static HourType getHourType(String softwareToken, String code) throws SQLException {
		HourType t = null;
		PreparedStatement stmt = null;
		Connection con = DBConnection.createDatabaseConnection(true);
		try {
			String selectSQL = "SELECT * FROM hourtypes WHERE softwareToken = ? AND code = ?";
			stmt = con.prepareStatement(selectSQL);
			stmt.setString(1, softwareToken);
			stmt.setString(2, code);
			ResultSet output = stmt.executeQuery();
			while (output.next()) {
				Double price = output.getDouble("sale_price");
				String name = output.getString("name");
				String id = output.getString("id");
				String modified = output.getString("modified");
				t = new HourType(code, name, 0, 0, 0, price, 1, modified, id);
			}
			output.close();
			stmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return t;
	}
	
	public static int getAddressID(String softwareToken, String addressType, String codeString) {
		int a = 0;
		try {
			Connection con = DBConnection.createDatabaseConnection(true);
			Statement statement = con.createStatement();
			ResultSet output = statement.executeQuery("SELECT * FROM relations WHERE softwareToken =\"" + softwareToken
					+ "\" AND type=\"" + addressType + "\"AND code=\"" + codeString + "\"");
			if (output.next()) {
				String addressId = output.getString("addressId");
				a = Integer.parseInt(addressId);
			}
			output.close();
			statement.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return a;
		
	}
	
	public static void saveSettings(Settings set, String token) {
		PreparedStatement stmt = null;
		Connection con = null;
		if (set != null) {
			try {
				con = DBConnection.createDatabaseConnection(true);
				stmt = con.prepareStatement(
						"REPLACE INTO settings (import_office, export_office, factuur_type, import_types, user, werkbon_type, rounded_hours, export_types, sync_date, softwareToken) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
				stmt.setString(1, set.getImportOffice());
				stmt.setString(2, set.getExportOffice());
				stmt.setString(3, set.getFactuurType());
				stmt.setString(4, set.getImportObjects() + "");
				if (set.getUser() == null) {
					stmt.setString(5, set.getMaterialCode());
				} else {
					stmt.setString(5, set.getUser());
				}
				stmt.setString(6, set.getExportWerkbontype());
				stmt.setInt(7, set.getRoundedHours());
				stmt.setString(8, set.getExportObjects() + "");
				stmt.setString(9, set.getSyncDate());
				stmt.setString(10, token);
				stmt.executeUpdate();
				stmt.close();
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static Settings getSettings(String softwareToken) {
		Settings set = null;
		Connection con = null;
		try {
			con = DBConnection.createDatabaseConnection(true);
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
				String user = output.getString("user");
				String exportWerkbonType = output.getString("werkbon_type");
				int roundedHours = output.getInt("rounded_hours");
				String exportTypes = output.getString("export_types");
				ArrayList<String> allExportTypes = null;
				if (exportTypes != null) {
					exportTypes = exportTypes.replace("]", "");
					exportTypes = exportTypes.replace("[", "");
					String[] strValues1 = exportTypes.split(",\\s");
					allExportTypes = new ArrayList<String>(Arrays.asList(strValues1));
				}
				String syncDate = output.getString("sync_date");
				set = new Settings(importOffice, exportOffice, factuurType, allTypes, user, exportWerkbonType,
						roundedHours, syncDate, user, allExportTypes);
			}
			output.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return set;
	}
	
	public static ArrayList<Map<String, String>> getLogs(String softwareToken) {
		ArrayList<Map<String, String>> allLogs = null;
		Connection con = null;
		try {
			con = DBConnection.createDatabaseConnection(true);
			allLogs = new ArrayList<Map<String, String>>();
			Statement statement = con.createStatement();
			ResultSet output = statement
					.executeQuery("SELECT * FROM log WHERE softwareToken =\"" + softwareToken + "\"");
			while (output.next()) {
				String timestamp = output.getString("timestamp");
				String messageString = output.getString("message");
				String details = output.getString("details");
				Map<String, String> logMap = new HashMap<String, String>();
				logMap.put("timestamp", timestamp);
				logMap.put("message", messageString);
				logMap.put("details", details);
				allLogs.add(logMap);
			}
			output.close();
			statement.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Collections.reverse(allLogs);
		
		return allLogs;
	}
	
	public static void saveLog(String log, String details, String token) {
		// sys date
		ZonedDateTime za = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
		String timestamp = za.format(formatter);
		
		// delete old logs
		deleteLog(token);
		PreparedStatement stmt = null;
		try {
			Connection con = DBConnection.createDatabaseConnection(true);
			stmt = con.prepareStatement(
					"REPLACE INTO log (message, details, timestamp, softwareToken) values (?, ?, ?, ?)");
			stmt.setString(1, log);
			stmt.setString(2, details);
			stmt.setString(3, timestamp);
			stmt.setString(4, token);
			stmt.executeUpdate();
			
			stmt.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void deleteLog(String token) {
		ArrayList<Map<String, String>> allLogs = getLogs(token);
		// sys date
		ZonedDateTime za = ZonedDateTime.now(ZoneId.of("Europe/Paris"));
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
		Date currentTime;
		Connection con = null;
		try {
			con = DBConnection.createDatabaseConnection(true);
			currentTime = format.parse(za.format(formatter));
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
			statement.close();
			
		} catch (ParseException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Boolean hasContent(String softwareToken, String table) throws SQLException {
		Boolean b = false;
		Connection con = DBConnection.createDatabaseConnection(true);
		Statement statement = con.createStatement();
		ResultSet output = null;
		output = statement.executeQuery("SELECT * FROM " + table + " WHERE softwareToken =\"" + softwareToken + "\"");
		while (output.next()) {
			b = true;
		}
		statement.close();
		output.close();
		return b;
	}
	
	public static String getModifiedDate(String softwareToken, String type, String code, String table)
			throws SQLException {
		String modified = null;
		Connection con = DBConnection.createDatabaseConnection(true);
		Statement statement = con.createStatement();
		ResultSet output = null;
		switch (table) {
		case "materials":
			output = statement.executeQuery("SELECT modified FROM " + table + " WHERE softwareToken =\"" + softwareToken
					+ "\" AND code =\"" + code + "\"");
			break;
		case "relations":
			output = statement.executeQuery("SELECT modified FROM " + table + " WHERE softwareToken =\"" + softwareToken
					+ "\" AND type =\"" + type + "\" AND code =\"" + code + "\"");
			break;
		case "hourtypes":
			output = statement.executeQuery("SELECT modified FROM " + table + " WHERE softwareToken =\"" + softwareToken
					+ "\" AND code =\"" + code + "\"");
			break;
		case "projects":
			output = statement.executeQuery("SELECT modified FROM " + table + " WHERE softwareToken =\"" + softwareToken
					+ "\" AND code =\"" + code + "\"");
			break;
		}
		
		while (output.next()) {
			modified = output.getString("modified");
		}
		statement.close();
		output.close();
		return modified;
	}
}
