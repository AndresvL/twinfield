package controller.wefact;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import DAO.ObjectDAO;
import controller.WorkOrderHandler;
import object.workorder.Address;
import object.workorder.Material;
import object.workorder.Relation;
import object.workorder.WorkOrder;

public class WeFactHandler {
	private String controller, action;
	String array  = null;

	public HttpURLConnection getConnection(int postDataLength) throws IOException {
		String link = "https://www.mijnwefact.nl/apiv2/api.php";
		URL url = new URL(link);

		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setInstanceFollowRedirects(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("charset", "utf-8");
		conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
		conn.setUseCaches(false);
		conn.connect();
		return conn;
	}

	public Boolean checkClientToken(String clientToken) throws IOException {
		String status;
		Boolean b = false;
		// Api key, debtor and list as default to check if api key is
		// authenticated at WeFact
		controller = "debtor";
		action = "list";
		JSONObject json = null;
		try {
			json = getJsonResponse(clientToken, controller, action, null);
			status = json.getString("status");
			if (status.equals("success")) {
				b = true;
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return b;

	}

	public JSONObject getJsonResponse(String clientToken, String controller, String action, String array)
			throws IOException, JSONException {
		String jsonString;
		String parameters = null;
		if (array == null) {
			parameters = "api_key=" + clientToken + "&controller=" + controller + "&action=" + action;
		} else {
			parameters = "api_key=" + clientToken + "&controller=" + controller + "&action=" + action + array;
		}
		System.out.println("param " + parameters);
		byte[] postData = parameters.getBytes(StandardCharsets.UTF_8);
		int postDataLength = postData.length;
		// Sets up the rest call;
		HttpURLConnection conn = getConnection(postDataLength);
		try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
			wr.write(postData);
		}
		BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream()), StandardCharsets.UTF_8));
		JSONObject json = null;
		while ((jsonString = br.readLine()) != null) {
			json = new JSONObject(jsonString);
		}
		return json;
	}
	
	public String getDateMinHour(String string){
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = null;
		try {
			//String to date
			date = format.parse(string);
			//Create Calender to edit time
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.HOUR_OF_DAY, -2);
			date = cal.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		//Date to String
		Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String s = formatter.format(date);
		return s ;
	}
	
	public String getHourtypes(String clientToken, String softwareToken) throws Exception {
		return softwareToken;
	}
	
	//Producten
	public String getMaterials(String clientToken, String softwareToken, String date) throws Exception {
		String errorMessage = "";
		controller = "product";
		action = "list";
		int importCount = 0;
		int editCount = 0;
		ArrayList<Material> materials = new ArrayList<Material>();
		Boolean hasContent =  ObjectDAO.hasContent(softwareToken, "materials");
		if(date != null){
			array = "&modified[from]=" + getDateMinHour(date);
		}
		if(!hasContent){
			array = null;
		}
		JSONObject jsonList = getJsonResponse(clientToken, controller, action, array);
		System.out.println(jsonList);
		String status = jsonList.getString("status");
		int totalResults = jsonList.getInt("totalresults");
		if (status.equals("success")&& totalResults > 0) {
			JSONArray products = jsonList.getJSONArray("products");
			// Check if request is successful
			for (int i = 0; i < products.length(); i++) {				
				JSONObject object = products.getJSONObject(i);
				String modified = object.getString("Modified");
				String productCode = object.getString("ProductCode");
				String dbModified = ObjectDAO.getModifiedDate(softwareToken, null, productCode, "materials");
				// Check if data is modified
				if (!modified.equals(dbModified)) {
					if (dbModified == null) {
						importCount++;
					} else {
						editCount++;
					}
					String description = object.getString("ProductName");
					Double price = object.getDouble("PriceExcl");
					String unit = object.getString("NumberSuffix");
					Material m = new Material(productCode, null, unit, description, price, null, modified);
					materials.add(m);
				}
			}
		}
		if (!materials.isEmpty()) {
			ObjectDAO.saveMaterials(materials, softwareToken);
			Boolean b = WorkOrderHandler.addData(softwareToken, materials, "materials");
			if (b) {
				errorMessage += "Success " + importCount + " materials imported<br />";
				errorMessage += "and " + editCount + " materials edited<br />";
			} else {
				errorMessage += "Something went wrong with Materials<br />";
			}
		} else {
			errorMessage += "No materials for import<br />";
		}
		
		return errorMessage;
	}

	// Debiteuren
	public String getRelations(String clientToken, String softwareToken, String date) throws Exception {
		String errorMessage = "";
		Relation r = null;
		controller = "debtor";
		action = "list"; 
		ArrayList<Relation> relations = new ArrayList<Relation>();
		Boolean hasContent =  ObjectDAO.hasContent(softwareToken, "relations");
		
		if(date != null){
			array = "&modified[from]=" + getDateMinHour(date);
		}
		if(!hasContent){
			array = null;
		}
		// Get all debtorCodes in WeFact
		JSONObject jsonList = getJsonResponse(clientToken, controller, action, array);
		int importCount = 0;
		int editCount = 0;
		int totalResults = jsonList.getInt("totalresults");
		String status = jsonList.getString("status");
		System.out.println("jsonList " + jsonList);
		if (status.equals("success")&& totalResults > 0) {
			JSONArray debtors = jsonList.getJSONArray("debtors");
			for (int i = 0; i < debtors.length(); i++) {
				JSONObject object = debtors.getJSONObject(i);
				String debtorCode = object.getString("DebtorCode");
				action = "show";
				String array = "&DebtorCode=" + debtorCode;
				JSONObject jsonShow = getJsonResponse(clientToken, controller, action, array);
				ArrayList<Address> address = new ArrayList<Address>();
				String statusShow = jsonShow.getString("status");
				// Check if request is successful
				if (statusShow.equals("success")) {
					JSONObject debtorDetails = jsonShow.getJSONObject("debtor");
					String modified = debtorDetails.getString("Modified");
					String debtorNr = debtorDetails.getString("DebtorCode");
					String dbModified = ObjectDAO.getModifiedDate(softwareToken, "postal", debtorNr, "relations");
					// Check if data is modified
					if (!modified.equals(dbModified)) {
						if (dbModified == null) {
							importCount++;
						} else {
							editCount++;
						}

						// Postal
						String firstName = debtorDetails.getString("Initials");
						String lastName = debtorDetails.getString("SurName");
						String companyName = debtorDetails.getString("CompanyName");
						String contact = firstName + " " + lastName;
						String mobileNr = debtorDetails.getString("MobileNumber");
						String phoneNr = debtorDetails.getString("PhoneNumber");
						if (phoneNr.equals("")) {
							phoneNr = mobileNr;
						}
						String email = debtorDetails.getString("EmailAddress");
						if (email.equals("")) {
							email = "leeg";
						}
						String houseNumber = "";
						String street = "";
						String streetNumber[] = debtorDetails.getString("Address").split("\\s+");
						for (int j = 0; j < streetNumber.length; j++) {
							if (j == streetNumber.length - 1) {
								houseNumber = streetNumber[j];
							} else if (j == streetNumber.length - 2) {
								street += streetNumber[j];
							} else {
								street += streetNumber[j] + " ";
							}
						}
						if (street.equals("")) {
							street = "leeg";
						}
						String postalCode = debtorDetails.getString("ZipCode");
						if (postalCode.equals("")) {
							postalCode = "leeg";
						}
						String city = debtorDetails.getString("City");
						if (city.equals("")) {
							city = "leeg";
						}
						String remark = debtorDetails.getString("Comment");

						Address postal = new Address(contact, phoneNr, email, street, houseNumber, postalCode, city,
								remark, "postal", 2);
						// Invoice
						String invoiceFirstName = debtorDetails.getString("InvoiceInitials");
						String invoiceLastName = debtorDetails.getString("InvoiceSurName");
						String invoiceContact = invoiceFirstName + " " + invoiceLastName;
						String invoiceCompanyName = debtorDetails.getString("InvoiceCompanyName");

						String invoiceEmail = debtorDetails.getString("InvoiceEmailAddress");
						if (invoiceEmail.equals("")) {
							invoiceEmail = email;
						}
						String invoicehouseNumber = "";
						String invoicestreet = "";
						String invoicestreetNumber[] = debtorDetails.getString("InvoiceAddress").split("\\s+");
						for (int j = 0; j < invoicestreetNumber.length; j++) {
							if (j == invoicestreetNumber.length - 1) {
								invoicehouseNumber = invoicestreetNumber[j];
							} else if (j == invoicestreetNumber.length - 2) {
								invoicestreet += invoicestreetNumber[j];
							} else {
								invoicestreet += invoicestreetNumber[j] + " ";
							}
						}
						if (invoicestreet.equals("")) {
							invoicestreet = "leeg";
						}
						String invoicepostalCode = debtorDetails.getString("InvoiceZipCode");
						if (invoicepostalCode.equals("")) {
							invoicepostalCode = "leeg";
						}
						String invoicecity = debtorDetails.getString("InvoiceCity");
						if (invoicecity.equals("")) {
							invoicecity = "leeg";
						}
						if (!invoicecity.equals("leeg")) {
							Address invoice = new Address(invoiceContact, phoneNr, invoiceEmail, invoicestreet,
									invoicehouseNumber, invoicepostalCode, invoicecity, remark, "invoice", 1);
							address.add(invoice);
						}
						address.add(postal);
						r = new Relation(companyName, debtorNr, contact, invoiceEmail, address, modified);
						relations.add(r);
					}
				}
			}
		}
		if (!relations.isEmpty()) {
			ObjectDAO.saveRelations(relations, softwareToken);
			Boolean b = WorkOrderHandler.addData(softwareToken, relations, "relations");
			if (b) {
				errorMessage += "Success " + importCount + " relations imported<br />";
				errorMessage += "and " + editCount + " relations edited<br />";
			} else {
				errorMessage += "Something went wrong with Relations<br />";
			}
		} else {
			errorMessage += "No relations for import<br />";
		}
		return errorMessage;
	}
	//Workorder -- offerte
	public String getOfferte(String clientToken, String softwareToken, String date)throws Exception{
		String errorMessage = "";
//		Offerte o = null;
		controller = "pricequote";
		action = "list"; 
		ArrayList<Relation> relations = new ArrayList<Relation>();
		Boolean hasContent =  ObjectDAO.hasContent(softwareToken, "relations");
		
		if(date != null){
			array = "&modified[from]=" + getDateMinHour(date);
		}
		if(!hasContent){
			array = null;
		}
		// Get all debtorCodes in WeFact
		JSONObject jsonList = getJsonResponse(clientToken, controller, action, array);
		int importCount = 0;
		int editCount = 0;
		int totalResults = jsonList.getInt("totalresults");
		String status = jsonList.getString("status");
		System.out.println("jsonList " + jsonList);
		if (status.equals("success")&& totalResults > 0) {
			JSONArray debtors = jsonList.getJSONArray("debtors");
			for (int i = 0; i < debtors.length(); i++) {
				JSONObject object = debtors.getJSONObject(i);
				String debtorCode = object.getString("DebtorCode");
				action = "show";
				String array = "&DebtorCode=" + debtorCode;
				JSONObject jsonShow = getJsonResponse(clientToken, controller, action, array);
			}
		}
		return status;
	}
}
