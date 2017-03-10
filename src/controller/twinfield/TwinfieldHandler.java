package controller.twinfield;

import java.io.IOException;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletException;

import DAO.ObjectDAO;
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

	public String[] getEmployees(String office, String session, String cluster, String token, String softwareName, String date)
			throws ServletException, IOException {
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
			ObjectDAO.saveEmployees(emp, token);
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

	public String[] getProjects(String office, String session, String cluster, String token, String softwareName, String date)
			throws ServletException, IOException {
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
			System.out.println("PROJECT NOT NULL");
			ObjectDAO.saveProjects(projects, token);
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

	public String[] getMaterials(String office, String session, String cluster, String token, String softwareName, String date)
			throws ServletException, IOException {
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
			ObjectDAO.saveMaterials(materials, token);
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

	public String[] getRelations(String office, String session, String cluster, String token, String softwareName, String date)
			throws ServletException, IOException {
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
			System.out.println("DebtorNr " + parts[0]);
			Object obj = SoapHandler.createSOAPXML(session, cluster, string, "relation");
			if (obj != null) {
				Relation r = (Relation) obj;
				relations.add(r);
			}
		}
		if (!relations.isEmpty()) {
			ObjectDAO.saveRelations(relations, token);
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

	public String[] getHourTypes(String office, String session, String cluster, String token, String softwareName, String date)
			throws ServletException, IOException {
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
			ObjectDAO.saveHourTypes(hourtypes, token);
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
	public String[] getWorkOrders(String office, String session, String cluster, String token, String factuurType, String softwareName) {
		ArrayList<WorkOrder> allData = WorkOrderHandler.getData(token, "GetWorkorders", factuurType, false,	softwareName);
		ArrayList<WorkOrder> tempUren = new ArrayList<WorkOrder>();
		if (allData.isEmpty() || allData == null) {
			System.out.println("allData is empty");
		}
		hourString = "<teqs>";
		for (WorkOrder w : allData) {
			if(allData.size() > 20){
				errorMessage += "Er zijn meer dan 20 werkbonnen gevonden met status compleet";
				return new String[] { errorMessage, "Neem contact op met WerkbonApp Support" };
			}
			// if projectnr is empty create an invoice
			if (w.getProjectNr().equals("")) {
				factuurAmount++;
				setFactuur(w, token, office, session, cluster, softwareName);

			} else {
				setUurboeking(w, office, tempUren);
			}
		}
		hourString += "</teqs>";
		
		//factuur error
		if (factuurAmount > 0) {
			// Factuur
			if (factuurSuccess > 0 && factuurError == 0) {
				errorMessage += factuurSuccess + " facturen aangemaakt<br />";
			} else if (factuurError > 0) {
				errorMessage += "Er ging iets mis met het verzenden van een factuur, klik voor meer details<br />";

			} else if (factuurError > 0 && factuurSuccess > 0) {
				errorMessage += factuurSuccess + " van de " + factuurAmount + " facturen verzonden<br />";

			}
			messageArray = new String[] { errorMessage, errorFactuurDetails };
		}
		//Uren error
		if (!hourString.equals("<teqs></teqs>")) {
			ArrayList<String> results = (ArrayList<String>) SoapHandler.createSOAPXML(session, cluster, hourString, "workorder");
			int urenSuccess = 0;
			for (String s : results) {
				if (s.equals("true")) {
					WorkOrder o = tempUren.get(urenSuccess);
					WorkOrderHandler.setWorkorderStatus(o.getId(), o.getWorkorderNr(), true, "GetWorkorder", token,	softwareName);
					urenSuccess++;
				} else {
					urenError++;
					errorUrenDetails += s;
				}
			}
			int urenAmount = urenSuccess + urenError;
			if (urenSuccess > 0 && urenError == 0) {
				errorMessage += urenSuccess + " uurboekingen aangemaakt<br />";
			} else if (results.size() > 0 && urenSuccess == 0) {
				errorMessage += "Er ging iets mis met het verzenden van de uurboeking, klik voor meer details<br />";
			} else if (urenError > 0 && urenSuccess > 0) {
				errorMessage += urenSuccess + " van de " + urenAmount + " facturen verzonden<br />";
			} else {
				errorMessage += "Geen uurboeking werkbonnen gevonden<br />";
			}
			messageArray = new String[] { errorMessage, errorUrenDetails };
		}
		for (int k = 0; k < messageArray.length; k++) {
			System.out.println("messageArray " + messageArray[k]);
		}
		return messageArray;
	}

	private void setFactuur(WorkOrder w, String token, String office, String session, String cluster, String softwareName) {
		Address factuur = null;
		Address post = null;
		post = ObjectDAO.getAddressID(token, "postal", w.getCustomerDebtorNr());
		factuur = ObjectDAO.getAddressID(token, "invoice", w.getCustomerDebtorNr());
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
					errorMessage += "Een materiaal op werkbon " + w.getWorkorderNr() + " bestaat niet in Twinfield<br />";
					factuurString = "";
				}
			}
			factuurString += "</lines></salesinvoice>";
			//If material exist
			if (!factuurString.equals("</lines></salesinvoice>")) {
				String resultsFactuur = (String) SoapHandler.createSOAPXML(session, cluster, factuurString, "workorderFactuur");
				if (resultsFactuur.equals("true")) {
					WorkOrderHandler.setWorkorderStatus(w.getId(), w.getWorkorderNr(), true, "GetWorkorder", token,
							softwareName);
					factuurSuccess++;
				} else {
					// twinfield soapResponse
					factuurError++;
					errorFactuurDetails += resultsFactuur;
				}
			}
		} else {
			errorMessage += "Een relatie op werkbon " + w.getWorkorderNr() + " bestaat niet in Twinfield<br />";
		}
	}

	private void setUurboeking(WorkOrder w, String office, ArrayList<WorkOrder> tempUren) {
		String code = "DIRECT";
		if (!w.getHourType().equals("") && !w.getEmployeeNr().equals("")) {
			tempUren.add(w);
			hourString += "<teq>" + "<header>" + "<office>" + office + "</office>"
			// Check this later
					+ "<code>" + code + "</code>" + "<user>" + w.getEmployeeNr() + "</user>" + "<date>"
					+ w.getWorkDate() + "</date>" + "<prj1>" + w.getProjectNr() + "</prj1>" + "<prj2>" + w.getHourType()
					+ "</prj2>" + "</header>" + "<lines>" + "<line type= \"TIME\">" + "<duration>" + w.getDuration()
					+ "</duration>" + "<description>" + w.getDescription() + "</description>" + "</line>"
					+ "<line type=\"QUANTITY\">" + "</line>" + "</lines></teq>";
		} else {
			urenError++;
			errorMessage += "Werkbon " + w.getWorkorderNr() + " heeft geen medewerker of werktype<br />";
		}

	}
}
