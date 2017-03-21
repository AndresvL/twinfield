package controller.twinfield;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletException;

import DAO.ObjectDAO;
import DBUtil.DBConnection;
import controller.WorkOrderHandler;
import object.twinfield.Search;
import object.workorder.Address;
import object.workorder.Employee;
import object.workorder.HourType;
import object.workorder.Material;
import object.workorder.Project;
import object.workorder.Relation;
import object.workorder.WorkOrder;

public class TwinfieldHandler {
	private ArrayList<String> responseArray = null;
	private String invoiceType;
	private Search searchObject = null;
	private String[][] options = null;
	private String errorMessage = "";
	private String[] messageArray = null;
	private Boolean checkUpdate = false;
	private int factuurSuccess, factuurError, factuurAmount;
	private String hourString;
	private String factuurString = null;
	private String errorFactuurDetails = "";
	private String errorUrenDetails = "";
	private int urenError = 0;
	private String oldProjectNr = null;

	public String getDateMinHour(String string) {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			// String to date
			date = format.parse(string);
			// Create Calender to edit time
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.HOUR_OF_DAY, -2);
			date = cal.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		// Date to String
		Format formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String s = formatter.format(date);
		return s;
	}

	public String[] getEmployees(String office, String session, String cluster, String token, String softwareName,
			String date, Connection con) throws ServletException, IOException, SQLException {
		// Create search object
		// Parameters: type, pattern, field, firstRow, maxRows, options
		options = new String[][] { { "ArrayOfString", "string", "office", office } };
		searchObject = new Search("USR", "*", 0, 1, 100, options);
		responseArray = SoapHandler.createSOAPFinder(session, cluster, searchObject);
		ArrayList<Employee> emp = new ArrayList<Employee>();
		// Split data from ArrayList
		for (int i = 0; i < responseArray.size(); i++) {
			String[] parts = responseArray.get(i).split(",");
			// firstName and Lastname are identical
			Employee e = new Employee(parts[1], parts[1], parts[0]);
			emp.add(e);
		}
		if (!emp.isEmpty()) {
			ObjectDAO.saveEmployees(emp, token, con);
			// Post data to WorkorderApp
			int successAmount = WorkOrderHandler.addData(token, emp, "employees", softwareName);
			if (successAmount > 0) {
				errorMessage += successAmount + " medewerkers geïmporteerd<br />";
				checkUpdate = false;
			} else {
				errorMessage += "Er ging iets mis met de medewerkers<br />";
			}
		}
		messageArray = new String[] { errorMessage, checkUpdate + "" };
		return messageArray;
	}

	public String[] getProjects(String office, String session, String cluster, String token, String softwareName,
			String date, Connection con) throws ServletException, IOException {
		// Create search object
		// Parameters: type, pattern, field, firstRow, maxRows, options
		if (date != null) {
			options = new String[][] { { "ArrayOfString", "string", "dimtype", "PRJ" },
					{ "ArrayOfString", "string", "office", office },
					{ "ArrayOfString", "string", "modifiedsince", getDateMinHour(date) } };
		} else {
			options = new String[][] { { "ArrayOfString", "string", "dimtype", "PRJ" },
					{ "ArrayOfString", "string", "office", office } };
		}
		searchObject = new Search("DIM", "*", 0, 1, 100, options);
		responseArray = SoapHandler.createSOAPFinder(session, cluster, searchObject);
		ArrayList<Project> projects = new ArrayList<Project>();
		for (int i = 0; i < responseArray.size(); i++) {
			String[] parts = responseArray.get(i).split(",");
			// XMLString
			String string = "<read><type>dimensions</type><office>" + office + "</office><dimtype>PRJ</dimtype><code>"
					+ parts[0] + "</code></read>";
			Object obj = SoapHandler.createSOAPXML(session, cluster, string, "project");
			if (obj != null) {
				Project p = (Project) obj;
				projects.add(p);

			}
		}
		if (!projects.isEmpty()) {
			ObjectDAO.saveProjects(projects, token, con);
			int successAmount = WorkOrderHandler.addData(token, projects, "projects", softwareName);
			if (successAmount > 0) {
				errorMessage += successAmount + " projecten geïmporteerd<br />";
				checkUpdate = true;
			} else {
				errorMessage += "Er ging iets mis met de projecten<br />";
			}
		}
		messageArray = new String[] { errorMessage, checkUpdate + "" };
		return messageArray;
	}

	public String[] getMaterials(String office, String session, String cluster, String token, String softwareName,
			String date, Connection con) throws ServletException, IOException {
		// Create search object
		// Parameters: type, pattern, field, firstRow, maxRows, options
		options = new String[][] { { "ArrayOfString", "string", "office", office } };
		searchObject = new Search("ART", "*", 0, 1, 100, options);
		responseArray = SoapHandler.createSOAPFinder(session, cluster, searchObject);
		ArrayList<Material> materials = new ArrayList<Material>();
		for (int i = 0; i < responseArray.size(); i++) {
			String[] parts = responseArray.get(i).split(",");
			String string = "<read><type>article</type><office>" + office + "</office><code>" + parts[0]
					+ "</code></read>";
			Object obj = SoapHandler.createSOAPXML(session, cluster, string, "material");
			if (obj != null) {
				@SuppressWarnings("unchecked")
				ArrayList<Material> subMaterial = (ArrayList<Material>) obj;
				for (Material sub : subMaterial) {
					materials.add(sub);
				}
			}
		}
		if (!materials.isEmpty()) {
			ObjectDAO.saveMaterials(materials, token, con);
			int successAmount = WorkOrderHandler.addData(token, materials, "materials", softwareName);
			if (successAmount > 0) {
				errorMessage += successAmount + " materialen geïmporteerd<br />";
				checkUpdate = false;
			} else {
				errorMessage += "Er ging iets mis met de materialen<br />";
			}
		}
		messageArray = new String[] { errorMessage, checkUpdate + "" };
		return messageArray;
	}

	public String[] getRelations(String office, String session, String cluster, String token, String softwareName,
			String date, Connection con) throws ServletException, IOException {
		// Create search object
		// Parameters: type, pattern, field, firstRow, maxRows, options
		if (date != null) {
			options = new String[][] { { "ArrayOfString", "string", "dimtype", "DEB" },
					{ "ArrayOfString", "string", "office", office },
					{ "ArrayOfString", "string", "modifiedsince", getDateMinHour(date) } };
		} else {
			options = new String[][] { { "ArrayOfString", "string", "dimtype", "DEB" },
					{ "ArrayOfString", "string", "office", office } };
		}
		searchObject = new Search("DIM", "*", 0, 1, 100, options);
		responseArray = SoapHandler.createSOAPFinder(session, cluster, searchObject);
		ArrayList<Relation> relations = new ArrayList<Relation>();
		for (int i = 0; i < responseArray.size(); i++) {
			String[] parts = responseArray.get(i).split(",");
			String string = "<read><type>dimensions</type><office>" + office + "</office><dimtype>DEB</dimtype><code>"
					+ parts[0] + "</code></read>";
			Object obj = SoapHandler.createSOAPXML(session, cluster, string, "relation");
			if (obj != null) {
				Relation r = (Relation) obj;
				relations.add(r);
			}
		}
		if (!relations.isEmpty()) {
			ObjectDAO.saveRelations(relations, token, con);
			int successAmount = WorkOrderHandler.addData(token, relations, "relations", softwareName);
			if (successAmount > 0) {
				errorMessage += successAmount + " relaties geïmporteerd<br />";
				checkUpdate = true;
			} else {
				errorMessage += "Er ging iets mis met de relaties<br />";
			}
		}
		messageArray = new String[] { errorMessage, checkUpdate + "" };
		return messageArray;
	}

	public String[] getHourTypes(String office, String session, String cluster, String token, String softwareName,
			String date, Connection con) throws ServletException, IOException {
		// Create search object
		// Parameters: type, pattern, field, firstRow, maxRows, options
		if (date != null) {
			options = new String[][] { { "ArrayOfString", "string", "office", office },
					{ "ArrayOfString", "string", "dimtype", "ACT" },
					{ "ArrayOfString", "string", "modifiedsince", getDateMinHour(date) } };
		} else {
			options = new String[][] { { "ArrayOfString", "string", "office", office },
					{ "ArrayOfString", "string", "dimtype", "ACT" } };
		}
		searchObject = new Search("DIM", "*", 0, 1, 100, options);
		responseArray = SoapHandler.createSOAPFinder(session, cluster, searchObject);
		ArrayList<HourType> hourtypes = new ArrayList<HourType>();
		for (int i = 0; i < responseArray.size(); i++) {
			String[] parts = responseArray.get(i).split(",");
			String string = "<read><type>dimensions</type><office>" + office + "</office><dimtype>ACT</dimtype><code>"
					+ parts[0] + "</code></read>";
			Object obj = SoapHandler.createSOAPXML(session, cluster, string, "hourtype");
			if (obj != null) {
				HourType h = (HourType) obj;
				hourtypes.add(h);
			}
		}
		if (!hourtypes.isEmpty()) {
			ObjectDAO.saveHourTypes(hourtypes, token, con);
			int successAmount = WorkOrderHandler.addData(token, hourtypes, "hourtypes", softwareName);
			if (successAmount > 0) {
				errorMessage += successAmount + " uursoorten geïmporteerd<br />";
				checkUpdate = true;
			} else {
				errorMessage += "Er ging iets mis met de uursoorten<br />";
			}
		}
		messageArray = new String[] { errorMessage, checkUpdate + "" };
		return messageArray;
	}

	@SuppressWarnings("unchecked")
	public String[] getWorkOrders(String office, String session, String cluster, String token, String factuurType,
			String softwareName, String user) throws SQLException {
		ArrayList<WorkOrder> allData = WorkOrderHandler.getData(token, "GetWorkorders", factuurType, false,
				softwareName);
		Connection con = DBConnection.createDatabaseConnection();
		ArrayList<WorkOrder> tempUren = new ArrayList<WorkOrder>();
		if (allData.isEmpty() || allData == null) {
			System.out.println("allData is empty");
		}
		hourString = "<teqs>";
		for (WorkOrder w : allData) {
			if (w.getProjectNr().equals("")) {
				factuurAmount++;
				setFactuur(w, token, office, session, cluster, softwareName, con);
			} else {
				// set hourString
				setUurboeking(w, office, tempUren, user);
			}
		}
		con.close();
		hourString += "</teqs>";
		//-- LOG MESSAGES --
		// Factuur log
		if (factuurAmount > 0) {
			if (factuurError > 0) {
				if(factuurSuccess > 0){
					errorMessage += factuurSuccess + " facturen aangemaakt<br />";
				}
				errorMessage += factuurError + " van de " + factuurAmount + " werkbonnen voor facturatie hebben errors<br />";
//				errorFactuurDetails += "Zorg dat <b>alleen</b> de werkbonnen die je wilt <b>factureren</b> op status <b>compleet</b> staan";
			} else {
				// Factuur
				if (factuurSuccess > 0) {
					errorMessage += factuurSuccess + " facturen aangemaakt<br />";
				} else {
					errorMessage += "Geen werkbonnen voor facturatie gevonden<br />";
				}
			}
		}
		// Uren log
		if (!hourString.equals("<teqs></teqs>")) {
			ArrayList<String> results = (ArrayList<String>) SoapHandler.createSOAPXML(session, cluster, hourString,
					"workorder");
			int urenSuccess = 0;
			for (String s : results) {
				if (s.equals("true")) {
					WorkOrder o = tempUren.get(urenSuccess);
					WorkOrderHandler.setWorkorderStatus(o.getId(), o.getWorkorderNr(), true, "GetWorkorder", token,
							softwareName);
					urenSuccess++;
				} else {
					urenError++;
					errorUrenDetails += s;
				}
			}
			int urenAmount = urenSuccess + urenError;
			if (urenError > 0) {
//				errorUrenDetails += "Zorg dat <b>alleen</b> de werkbonnen waarvan je de uren wilt boeken op status <b>compleet</b> staan";
					if(urenSuccess > 0){
						errorMessage += urenSuccess + " uurboekingen aangemaakt<br />";
					}
					errorMessage += urenError + " van de " + urenAmount + " uurboekingen hebben errors<br />";
			} else {
				if (urenSuccess > 0) {
					errorMessage += urenSuccess + " uurboekingen aangemaakt<br />";
				} else {
					errorMessage += "Geen werkbonnen waarvan je de uren wilt boeken gevonden<br />";
				}
			}
		}
		// for (int k = 0; k < messageArray.length; k++) {
		// System.out.println("messageArray " + messageArray[k]);
		// }
		String details = errorFactuurDetails + errorUrenDetails;
		System.out.println("DETAILS " + details);
		return new String[] { errorMessage, details };
	}

	private void setFactuur(WorkOrder w, String token, String office, String session, String cluster,
			String softwareName, Connection con) throws SQLException {
		Address factuur = null;
		Address post = null;
		
		post = ObjectDAO.getAddressID(token, "postal", w.getCustomerDebtorNr(), con);
		factuur = ObjectDAO.getAddressID(token, "invoice", w.getCustomerDebtorNr(), con);
		if (post == null) {
			post = factuur;
		} else if (factuur == null) {
			factuur = post;
		}
		// + "<performancedate>" + w.getWorkDate() +
		// "</performancedate>"
		if (factuur != null) {
			invoiceType = "FACTUUR";
			factuurString = "<salesinvoice>" + "<header>" + "<office>" + office + "</office>" + "<invoicetype>"
					+ invoiceType + "</invoicetype>" + "<invoicedate>" + w.getCreationDate() + "</invoicedate>"
					+ "<customer>" + w.getCustomerDebtorNr() + "</customer>" + "<status>" + w.getStatus() + "</status>"
					+ "<paymentmethod>" + w.getPaymentMethod() + "</paymentmethod>" + "<invoiceaddressnumber>"
					+ factuur.getAddressId() + "</invoiceaddressnumber>" + "<deliveraddressnumber>"
					+ post.getAddressId() + "</deliveraddressnumber>" + "</header>" + "<lines>";
			int i = 0;
			for (Material m : w.getMaterials()) {
				String sub;
				if (m != null) {
					i++;
					if (m.getSubCode() == null) {
						sub = "";
					} else {
						sub = m.getSubCode();
					}
					factuurString += "<line id=\"" + i + "\">" + "<article>" + m.getCode() + "</article>"
							+ "<subarticle>" + sub + "</subarticle>" + "<quantity>" + m.getQuantity() + "</quantity>"
							+ "<units>" + m.getUnit() + "</units>" + "</line>";
				} else {
					errorMessage += "Een materiaal op werkbon " + w.getWorkorderNr()
							+ " bestaat niet in Twinfield<br />";
					factuurString = "";
				}
			}
			factuurString += "</lines></salesinvoice>";
			// If material exist
			if (!factuurString.equals("</lines></salesinvoice>")) {
				String resultsFactuur = (String) SoapHandler.createSOAPXML(session, cluster, factuurString,
						"workorderFactuur");
				if (resultsFactuur.equals("true")) {
					WorkOrderHandler.setWorkorderStatus(w.getId(), w.getWorkorderNr(), true, "GetWorkorder", token,
							softwareName);
					factuurSuccess++;
				} else {
					// twinfield soapResponse
					factuurError++;
					if (resultsFactuur != null) {
						errorFactuurDetails += resultsFactuur;
					}
				}
			}
		} else {
			factuurError++;
//			if (factuurError < 0) {
//				errorMessage += "Een relatie op werkbon " + w.getWorkorderNr() + " bestaat niet in Twinfield<br />";
////				errorFactuurDetails += "De synchronisatie van de relaties moet aangevinkt zijn";
//			}
		}
	}

	private void setUurboeking(WorkOrder w, String office, ArrayList<WorkOrder> tempUren, String user) {
		String code = "DIRECT";
		if (!w.getHourType().equals("")) {
			if (user.equals("Geen")) {
				user = w.getEmployeeNr();
			}
			if (!user.equals("") && user != null) {
				tempUren.add(w);
				hourString += "<teq>" + "<header>" + "<office>" + office + "</office>"
				// Check this later
						+ "<code>" + code + "</code>" + "<user>" + user + "</user>" + "<date>" + w.getWorkDate()
						+ "</date>" + "<prj1>" + w.getProjectNr() + "</prj1>" + "<prj2>" + w.getHourType() + "</prj2>"
						+ "</header>" + "<lines>" + "<line type= \"TIME\">" + "<duration>" + w.getDuration()
						+ "</duration>" + "<description>" + w.getDescription() + "</description>" + "</line>"
						+ "<line type=\"QUANTITY\">" + "</line>" + "</lines></teq>";
			} else {
				urenError++;
				if (urenError < 5) {
					errorMessage += "Een werkbon met projectnummer " + w.getProjectNr()
							+ " heeft een werkperiode zonder medewerker<br />";
				}
			}
		} else {
			urenError++;
			//Filter the results a little bit
//			if (!w.getProjectNr().equals(oldProjectNr)) {
//				if (urenError < 5) {
//					errorMessage += "Een werkbon met projectnummer " + w.getProjectNr()
//							+ " heeft een werkperiode zonder werktype<br />";
//					oldProjectNr = w.getProjectNr();
//				}
//			}
		}

	}
}
