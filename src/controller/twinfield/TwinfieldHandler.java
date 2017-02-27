package controller.twinfield;

import java.io.IOException;
import java.util.ArrayList;

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
	private String[][] options = null;
	private Search searchObject;
	private String errorMessage = "";
	
	public String getEmployees(String office, String session, String token) throws ServletException, IOException {
		// Create search object
		// Parameters: type, pattern, field, firstRow, maxRows, options
		options = new String[][] { { "ArrayOfString", "string", "office", office } };
		searchObject = new Search("USR", "*", 0, 1, 100, options);
		responseArray = SoapHandler.createSOAPFinder(session, searchObject);
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
			Boolean b = WorkOrderHandler.addData(token, emp, "employees");
			if (b) {
				errorMessage += "Employees imported<br />";
			} else {
				errorMessage += "Something went wrong with Employees<br />";
			}
		} else {
			errorMessage += "No Employees found<br />";
		}
		return errorMessage;
	}

	public String getProjects (String office, String session, String token) throws ServletException, IOException {
		// Create search object
		// Parameters: type, pattern, field, firstRow, maxRows, options
		options = new String[][] { { "ArrayOfString", "string", "dimtype", "PRJ" } };
		searchObject = new Search("DIM", "*", 0, 1, 100, options);
		responseArray = SoapHandler.createSOAPFinder(session, searchObject);
		ArrayList<Project> projects = new ArrayList<Project>();
		for (int i = 0; i < responseArray.size(); i++) {
			String[] parts = responseArray.get(i).split(",");
			// XMLString
			String string = "<read><type>dimensions</type><office>" + office + "</office><dimtype>PRJ</dimtype><code>"
					+ parts[0] + "</code></read>";
			Object obj = SoapHandler.createSOAPXML(session, string, "project");
			if (obj != null) {
				Project p = (Project) obj;
				projects.add(p);
			}
		}
		if (!projects.isEmpty()) {
			ObjectDAO.saveProjects(projects, token);
			Boolean b = WorkOrderHandler.addData(token, projects, "projects");
			if (b) {
				errorMessage += "Projects imported<br />";
			} else {
				errorMessage += "Something went wrong with Projects<br />";
			}
		} else {
			errorMessage += "No Projects found in office " + office + "<br />";
		}
		return errorMessage;
	}

	public String getMaterials(String office, String session, String token) throws ServletException, IOException {
		// Create search object
		// Parameters: type, pattern, field, firstRow, maxRows, options
		options = new String[][] { { "ArrayOfString", "string", "office", office } };
		searchObject = new Search("ART", "*", 0, 1, 100, options);
		responseArray = SoapHandler.createSOAPFinder(session, searchObject);
		ArrayList<Material> materials = new ArrayList<Material>();
		for (int i = 0; i < responseArray.size(); i++) {
			String[] parts = responseArray.get(i).split(",");
			String string = "<read><type>article</type><office>" + office + "</office><code>" + parts[0]
					+ "</code></read>";
			Object obj = SoapHandler.createSOAPXML(session, string, "material");
			if (obj != null) {
				@SuppressWarnings("unchecked")
				ArrayList<Material> subMaterial = (ArrayList<Material>) obj;
				for(Material sub : subMaterial){
					materials.add(sub);
				}
			}
		}
		if (!materials.isEmpty()) {
			ObjectDAO.saveMaterials(materials, token);
			Boolean b = WorkOrderHandler.addData(token, materials, "materials");
			if (b) {
				errorMessage += "Materials imported<br />";
			} else {
				errorMessage += "Something went wrong with Materials<br />";
			}
		} else {
			errorMessage += "No Materials found in office " + office + "<br />";
		}
		return errorMessage;
	}

	public String getRelations(String office, String session, String token) throws ServletException, IOException {
		// Create search object
		// Parameters: type, pattern, field, firstRow, maxRows, options
		options = new String[][] { { "ArrayOfString", "string", "dimtype", "DEB" } };
		searchObject = new Search("DIM", "*", 0, 1, 100, options);
		responseArray = SoapHandler.createSOAPFinder(session, searchObject);
		ArrayList<Relation> relations = new ArrayList<Relation>();
		for (int i = 0; i < responseArray.size(); i++) {
			String[] parts = responseArray.get(i).split(",");
			String string = "<read><type>dimensions</type><office>" + office + "</office><dimtype>DEB</dimtype><code>"
					+ parts[0] + "</code></read>";
			Object obj = SoapHandler.createSOAPXML(session, string, "relation");
			if (obj != null) {
				Relation r = (Relation) obj;
				relations.add(r);
			}
		}
		if (!relations.isEmpty()) {
			ObjectDAO.saveRelations(relations, token);
			Boolean b = WorkOrderHandler.addData(token, relations, "relations");
			if (b) {
				errorMessage += "Relations imported<br />";
			} else {
				errorMessage += "Something went wrong with Relations<br />";
			}
		} else {
			errorMessage += "No Relations found in office " + office + "<br />";
		}
		return errorMessage;
	}

	public String getHourTypes(String office, String session, String token) throws ServletException, IOException {
		// Create search object
		// Parameters: type, pattern, field, firstRow, maxRows, options
		options = new String[][] { { "ArrayOfString", "string", "office", office },
				{ "ArrayOfString", "string", "dimtype", "ACT" } };
		searchObject = new Search("DIM", "*", 0, 1, 100, options);
		responseArray = SoapHandler.createSOAPFinder(session, searchObject);
		ArrayList<HourType> hourtypes = new ArrayList<HourType>();
		for (int i = 0; i < responseArray.size(); i++) {
			String[] parts = responseArray.get(i).split(",");
			String string = "<read><type>dimensions</type><office>" + office + "</office><dimtype>ACT</dimtype><code>"
					+ parts[0] + "</code></read>";
			Object obj = SoapHandler.createSOAPXML(session, string, "hourtype");
			if (obj != null) {
				HourType h = (HourType) obj;
				hourtypes.add(h);
			}
		}
		if (!hourtypes.isEmpty()) {
			ObjectDAO.saveHourTypes(hourtypes, token);
			Boolean b = WorkOrderHandler.addData(token, hourtypes, "hourtypes");
			if (b) {
				errorMessage += "Hourtypes imported<br />";
			} else {
				errorMessage += "Something went wrong with Hourtypes<br />";
			}
		} else {
			errorMessage += "No Hourtypes found in office " + office + "<br />";
		}
		return errorMessage;
	}
	@SuppressWarnings("unchecked")
	public String getWorkOrders(String office, String session, String token, String factuurType) {
		ArrayList<WorkOrder> allData = WorkOrderHandler.getData(token, "GetWorkorders", factuurType, false);
		ArrayList<WorkOrder> tempUren = new ArrayList<WorkOrder>();
		int factuurAmount = 0;
		if (allData.isEmpty() || allData == null) {
			System.out.println("allData is empty");
		}
		String hourString = "<teqs>";
		String string = null;
		for (WorkOrder w : allData) {
			// if projectnr is empty create a invoice
			if (w.getProjectNr().equals("")) {
				Address factuur = null;
				Address post = null;
				post = ObjectDAO.getAddressID(token, "postal", w.getCustomerDebtorNr());
				factuur = ObjectDAO.getAddressID(token, "invoice", w.getCustomerDebtorNr());
				if (post == null) {
					post = factuur;
				}
				if (factuur == null) {
					factuur = post;
				}

				// + "<performancedate>" + w.getWorkDate() +
				// "</performancedate>"
				invoiceType = "FACTUUR";
				string = "<salesinvoice>" + "<header>" + "<office>" + office + "</office>" + "<invoicetype>"
						+ invoiceType + "</invoicetype>" + "<invoicedate>" + w.getCreationDate() + "</invoicedate>"
						+ "<customer>" + w.getCustomerDebtorNr() + "</customer>" + "<status>" + w.getStatus()
						+ "</status>" + "<paymentmethod>" + w.getPaymentMethod() + "</paymentmethod>"
						+ "<invoiceaddressnumber>" + factuur.getAddressId() + "</invoiceaddressnumber>"
						+ "<deliveraddressnumber>" + post.getAddressId() + "</deliveraddressnumber>" + "</header>"
						+ "<lines>";
				int i = 0;
				for (Material m : w.getMaterials()) {
					i++;
					// subCode is empty for now because werkbonapp
					// doesnt provide this function
					String subCode = "";
					string += "<line id=\"" + i + "\">" + "<article>" + m.getCode() + "</article>" + "<subarticle>"
							+ m.getSubCode() + "</subarticle>" + "<quantity>" + m.getQuantity() + "</quantity>" + "<units>"
							+ m.getUnit() + "</units>" + "</line>";
				}
				string += "</lines></salesinvoice>";
				Boolean resultsFactuur = (Boolean) SoapHandler.createSOAPXML(session, string, "workorderFactuur");
//				System.out.println("resultsFactuur " + resultsFactuur);
				if (resultsFactuur) {
					WorkOrderHandler.setWorkorderStatus(w.getId(), w.getWorkorderNr(), resultsFactuur, "GetWorkorder",
							token);
					factuurAmount++;
				}
			} else {
				String code = "PERSONAL";
				invoiceType = "UREN";
				System.out.println("employeenr " + w.getEmployeeNr());
				System.out.println("w.getHourType()" + w.getHourType());
				if (!w.getHourType().equals("") && !w.getEmployeeNr().equals("")) {
					
					tempUren.add(w);
					hourString += "<teq>" + "<header>" + "<office>" + office + "</office>"
					// Check this later
							+ "<code>" + code + "</code>" + "<user>" + w.getEmployeeNr() + "</user>" + "<date>"
							+ w.getWorkDate() + "</date>" + "<prj1>" + w.getProjectNr() + "</prj1>" + "<prj2>"
							+ w.getHourType() + "</prj2>" + "</header>" + "<lines>" + "<line type= \"TIME\">"
							+ "<duration>" + w.getDuration() + "</duration>" + "<description>" + w.getDescription()
							+ "</description>" + "</line>" + "<line type=\"QUANTITY\">" + "</line>" + "</lines></teq>";
				}
			}
		}
		hourString += "</teqs>";
		System.out.println("hourString" + hourString);
		// Uren
		ArrayList<Boolean> results = (ArrayList<Boolean>) SoapHandler.createSOAPXML(session, hourString, "workorder");
		int i = 0;
		for (Boolean b : results) {
			WorkOrder o = tempUren.get(i);
			WorkOrderHandler.setWorkorderStatus(o.getId(), o.getWorkorderNr(), b, "GetWorkorder", token);
			if (b) {
				i++;
			}

		}
		if (i != 0) {
			errorMessage += i + " uurboeking(en) created<br />";
		} else {
			errorMessage += "0 uurboekingen created<br />";
		}

		// Factuur
		if (factuurAmount > 0) {
			errorMessage += factuurAmount + " Invoices created<br />";
		} else {
			errorMessage += "0 Invoices created<br />";
		}
		return errorMessage;
	}
}
