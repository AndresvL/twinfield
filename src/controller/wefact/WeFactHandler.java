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
import object.workorder.HourType;
import object.workorder.Material;
import object.workorder.Relation;
import object.workorder.WorkOrder;
import object.workorder.WorkPeriod;

public class WeFactHandler {
	private String controller, action;
	private String array = null;
	final String softwareName = "WeFact";
	private Boolean checkUpdate = false;

	public HttpURLConnection getConnection(int postDataLength, String jsonRequest) throws IOException {
		String link = "https://www.mijnwefact.nl/apiv2/api.php";
		String contentType = null;
		URL url = new URL(link);
		if (jsonRequest == null) {
			contentType = "application/x-www-form-urlencoded";
		} else {
			contentType = "application/json";
		}
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setDoOutput(true);
		conn.setInstanceFollowRedirects(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", contentType);
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
			json = getJsonResponse(clientToken, controller, action, null, null);
			status = json.getString("status");
			if (status.equals("success")) {
				b = true;
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return b;
	}

	public JSONObject getJsonResponse(String clientToken, String controller, String action, String array,
			String jsonRequest) throws IOException, JSONException {
		String jsonString;
		String parameters = null;
		if (array == null) {
			parameters = "api_key=" + clientToken + "&controller=" + controller + "&action=" + action;
		} else {
			parameters = "api_key=" + clientToken + "&controller=" + controller + "&action=" + action + array;
		}
		// jsonRequest is filled when a set Method is called;
		if (jsonRequest != null) {
			parameters = jsonRequest;
		}
		byte[] postData = parameters.getBytes(StandardCharsets.UTF_8);
		int postDataLength = postData.length;
		// Sets up the rest call;
		HttpURLConnection conn = getConnection(postDataLength, jsonRequest);
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
		Format formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String s = formatter.format(date);
		return s;
	}

	// Producten
	public String[] getMaterials(String clientToken, String softwareToken, String date) throws Exception {
		String errorMessage = "";
		controller = "product";
		action = "list";
		int importCount = 0;
		int editCount = 0;
		ArrayList<Material> materials = new ArrayList<Material>();
		Boolean hasContent = ObjectDAO.hasContent(softwareToken, "materials");
		if (date != null) {
			array = "&modified[from]=" + getDateMinHour(date);
		}
		if (!hasContent) {
			array = null;
		}
		JSONObject jsonList = getJsonResponse(clientToken, controller, action, array, null);
		String status = jsonList.getString("status");
		int totalResults = jsonList.getInt("totalresults");
		if (status.equals("success") && totalResults > 0) {
			JSONArray products = jsonList.getJSONArray("products");
			// Check if request is successful
			for (int i = 0; i < products.length(); i++) {
				JSONObject object = products.getJSONObject(i);
				String modified = object.getString("Modified");
				String productCode = object.getString("ProductCode");
				String dbModified = ObjectDAO.getModifiedDate(softwareToken, null, productCode, "materials");
				// Check if data is modified
				if (dbModified == null || array == null) {
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
		if (!materials.isEmpty()) {
			ObjectDAO.saveMaterials(materials, softwareToken);
			int successAmount = (int) WorkOrderHandler.addData(softwareToken, materials, "materials", softwareName,
					null);
			if (successAmount > 0) {
				errorMessage += "Success " + importCount + " materials imported<br />";
				errorMessage += "and " + editCount + " materials edited<br />";
				checkUpdate = true;
			} else {
				errorMessage += "Something went wrong with Materials<br />";
			}
		} else {
			errorMessage += "No materials for import<br />";
		}

		return new String[] { errorMessage, checkUpdate + "" };
	}

	// Debiteuren
	public String[] getRelations(String clientToken, String softwareToken, String date) throws Exception {
		String errorMessage = "";
		Relation r = null;
		controller = "debtor";
		action = "list";
		ArrayList<Relation> relations = new ArrayList<Relation>();
		Boolean hasContent = ObjectDAO.hasContent(softwareToken, "relations");
		if (date != null) {
			array = "&modified[from]=" + getDateMinHour(date);
		}
		if (!hasContent) {
			array = null;
		}
		// Get all debtorCodes in WeFact
		JSONObject jsonList = getJsonResponse(clientToken, controller, action, array, null);
		int importCount = 0;
		int editCount = 0;
		int totalResults = jsonList.getInt("totalresults");
		String status = jsonList.getString("status");
		// Check if ListRequest is successful
		if (status.equals("success") && totalResults > 0) {
			JSONArray debtors = jsonList.getJSONArray("debtors");
			for (int i = 0; i < debtors.length(); i++) {
				JSONObject object = debtors.getJSONObject(i);
				String debtorCode = object.getString("DebtorCode");
				action = "show";
				String debtorArray = "&DebtorCode=" + debtorCode;
				JSONObject jsonShow = getJsonResponse(clientToken, controller, action, debtorArray, null);
				ArrayList<Address> address = new ArrayList<Address>();
				String statusShow = jsonShow.getString("status");
				// Check if ShowRequest is successful
				if (statusShow.equals("success")) {
					JSONObject debtorDetails = jsonShow.getJSONObject("debtor");
					String modified = debtorDetails.getString("Modified");
					String debtorNr = debtorDetails.getString("DebtorCode");
					String dbModified = ObjectDAO.getModifiedDate(softwareToken, "postal", debtorNr, "relations");
					// Check if data is modified
					if (dbModified == null || array == null) {
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

					Address postal = new Address(contact, phoneNr, email, street, houseNumber, postalCode, city, remark,
							"postal", 2);
					address.add(postal);
					// Invoice
					String invoiceFirstName = debtorDetails.getString("InvoiceInitials");
					String invoiceLastName = debtorDetails.getString("InvoiceSurName");
					String invoiceContact = invoiceFirstName + " " + invoiceLastName;
					String invoiceCompanyName = debtorDetails.getString("InvoiceCompanyName");
					if(!invoiceCompanyName.equals("")){
						companyName = invoiceCompanyName;
					}
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

					r = new Relation(companyName, debtorNr, contact, invoiceEmail, address, modified);
					relations.add(r);
				}
			}
		}
		if (!relations.isEmpty()) {
			ObjectDAO.saveRelations(relations, softwareToken);
			int successAmount = (int) WorkOrderHandler.addData(softwareToken, relations, "relations", softwareName,
					null);
			if (successAmount > 0) {
				errorMessage += "Success " + importCount + " relations imported<br />";
				errorMessage += "and " + editCount + " relations edited<br />";
				checkUpdate = true;
			} else {
				errorMessage += "Something went wrong with Relations<br />";
			}
		} else {
			errorMessage += "No relations for import<br />";
		}
		return new String[] { errorMessage, checkUpdate + "" };
	}

	// Productengroep uren
	public String[] getHourTypes(String clientToken, String softwareToken, String date) throws Exception {
		String errorMessage = "";
		HourType h = null;
		controller = "group";
		action = "list";
		array = "&type=product";
		ArrayList<HourType> hourtypes = new ArrayList<HourType>();
		Boolean hasContent = ObjectDAO.hasContent(softwareToken, "hourtypes");
		// Get all groups in WeFact
		JSONObject jsonList = getJsonResponse(clientToken, controller, action, array, null);
		int importCount = 0;
		int editCount = 0;
		int totalResults = jsonList.getInt("totalresults");
		String status = jsonList.getString("status");
		// Check if ListRequest is successful
		if (status.equals("success") && totalResults > 0) {
			JSONArray groups = jsonList.getJSONArray("groups");
			for (int i = 0; i < groups.length(); i++) {
				JSONObject group = groups.getJSONObject(i);
				String id = group.getString("Identifier");
				String groupName = group.getString("GroupName");
				// HARDCODED UURSOORTEN
				if (groupName.equals("Uursoorten")) {
					controller = "product";
					action = "list";
					array = "&group=" + id;
					if (date != null) {
						array = "&group=" + id + "&modified[from]=" + getDateMinHour(date);
					}
					if (!hasContent) {
						array = "&group=" + id;
					}
					jsonList = getJsonResponse(clientToken, controller, action, array, null);
					String productStatus = jsonList.getString("status");
					totalResults = jsonList.getInt("totalresults");
					if (productStatus.equals("success") && totalResults > 0) {
						JSONArray products = jsonList.getJSONArray("products");
						// Check if request is successful
						for (int j = 0; j < products.length(); j++) {
							JSONObject object = products.getJSONObject(j);
							String modified = object.getString("Modified");
							String productCode = object.getString("ProductCode");
							String productName = object.getString("ProductName");
							String dbModified = ObjectDAO.getModifiedDate(softwareToken, null, productCode,
									"hourtypes");
							// Check if data is modified
							if (dbModified == null || date == null) {
								importCount++;
							} else {
								editCount++;
							}
							Double costPrice = object.getDouble("PriceExcl");
							Double tax = object.getDouble("TaxPercentage");
							// Calculate salesPrice with tax and productPrice
							Double salePrice = costPrice * (tax / 100 + 1.00);
							h = new HourType(productCode, productName, 0, 0, costPrice, salePrice, 1, modified);
							hourtypes.add(h);
						}
					}
				}
			}
		}

		if (!hourtypes.isEmpty()) {
			// Save to db
			ObjectDAO.saveHourTypes(hourtypes, softwareToken);
			// Send to WorkOrderApp
			int successAmount = (int) WorkOrderHandler.addData(softwareToken, hourtypes, "hourtypes", softwareName,
					null);
			if (successAmount > 0) {
				errorMessage += "Success " + importCount + " hourtypes imported<br />";
				errorMessage += "and " + editCount + " hourtypes edited<br />";
				checkUpdate = true;
			} else {
				errorMessage += "Something went wrong with hourtypes<br />";
			}
		} else {
			errorMessage += "No hourtypes for import<br />";
		}
		return new String[] { errorMessage, checkUpdate + "" };
	}

	// Workorder -- offerte
	public String[] getOffertes(String clientToken, String softwareToken, String date) throws Exception {
		String errorMessage = "";
		// Offerte o = null;
		controller = "pricequote";
		action = "list";
		array = "&status=3";
		// int importCount = 0;
		// int editCount = 0;
		ArrayList<WorkOrder> offertes = new ArrayList<WorkOrder>();
		// CHANGE LATER
		// Boolean hasContent = ObjectDAO.hasContent(softwareToken, "offertes");
		if (date != null) {
			array += "&modified[from]=" + getDateMinHour(date);
		}
		JSONObject jsonList = getJsonResponse(clientToken, controller, action, array, null);
		String status = jsonList.getString("status");
		int totalResults = jsonList.getInt("totalresults");
		// Check if ListRequest is successful
		if (status.equals("success") && totalResults > 0) {
			JSONArray offerteList = jsonList.getJSONArray("pricequotes");
			for (int i = 0; i < offerteList.length(); i++) {
				JSONObject offerteObject = offerteList.getJSONObject(i);
				String offerteNr = offerteObject.getString("PriceQuoteCode");
				action = "show";
				String offerteArray = "&PriceQuoteCode=" + offerteNr;
				JSONObject jsonShow = getJsonResponse(clientToken, controller, action, offerteArray, null);
				String statusShow = jsonShow.getString("status");
				// custom fields
				JSONObject offerteDetails = jsonShow.getJSONObject("pricequote");
				JSONObject customFields = offerteDetails.getJSONObject("CustomFields");
				int werkbonApp = customFields.getInt("werkbonapp");
				int werkbonAppAdded = customFields.getInt("werkbonappadded");
				String typeOfWork = customFields.getString("typeofwork");
				String paymentMethod = customFields.getString("paymentmethod");
				if (typeOfWork == null) {
					typeOfWork = "leeg";
				}
				// 3 is geaccepteerd
				int offerteStatus = offerteDetails.getInt("Status");
				// if customField werkbonapp is set to yes
				if (statusShow.equals("success") && werkbonApp == 1 && werkbonAppAdded == 0) {
					String modified = offerteDetails.getString("Modified");
					offerteNr = offerteDetails.getString("PriceQuoteCode");
					String debtorCode = offerteDetails.getString("DebtorCode");
					String workDate = null;
					// Map date
					try {
						SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
						Date formatDate = dt.parse(offerteDetails.getString("Date"));
						SimpleDateFormat dt1 = new SimpleDateFormat("dd-MM-yyyy");
						workDate = dt1.format(formatDate);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					String companyName = offerteDetails.getString("CompanyName");
					String id = offerteDetails.getString("Identifier");
					String initials = offerteDetails.getString("Initials");
					String lastName = offerteDetails.getString("SurName");
					String contact = initials + " " + lastName;
					// String houseNumber = "";
					String street = offerteDetails.getString("Address");
					if (street.equals("")) {
						street = "leeg";
					}
					String postalCode = offerteDetails.getString("ZipCode");
					if (postalCode.equals("")) {
						postalCode = "leeg";
					}
					String city = offerteDetails.getString("City");
					if (city.equals("")) {
						city = "leeg";
					}
					String email = offerteDetails.getString("EmailAddress");
					if (email.equals("")) {
						email = "leeg";
					}
					String description = offerteDetails.getString("Description");
					// String comment = offerteDetails.getString("Comment");
					// Invoice relation from database
					Relation dbRelation = ObjectDAO.getRelation(softwareToken, debtorCode, "invoice");
					if (dbRelation == null) {
						// Get postal address
						dbRelation = ObjectDAO.getRelation(softwareToken, debtorCode, "postal");
					}
					dbRelation.setCompanyName(companyName);
					dbRelation.setDebtorNumber(debtorCode);
					dbRelation.setContact(contact);
					dbRelation.setEmailWorkorder(email);

					ArrayList<Address> address = dbRelation.getAddressess();
					ArrayList<Address> offerteAddress = new ArrayList<Address>();
					// Always one address in array
					Address aObject = address.get(0);
					// change Address
					aObject.setStreet(street);
					aObject.setPostalCode(postalCode);
					aObject.setCity(city);
					offerteAddress.add(aObject);
					dbRelation.setAddresses(offerteAddress);
					ArrayList<Relation> allRelations = new ArrayList<Relation>();
					allRelations.add(dbRelation);

					JSONArray lines = offerteDetails.getJSONArray("PriceQuoteLines");
					ArrayList<Material> allMaterials = new ArrayList<Material>();
					for (int j = 0; j < lines.length(); j++) {
						JSONObject priceQuoteLineObject = lines.getJSONObject(j);
						String code = priceQuoteLineObject.getString("ProductCode");
						String unit = priceQuoteLineObject.getString("NumberSuffix");
						String materialDescription = priceQuoteLineObject.getString("Description");
						double price = priceQuoteLineObject.getDouble("PriceExcl");
						String quantity = priceQuoteLineObject.getString("Number");
						Material m = new Material(code, null, unit, materialDescription, price, quantity, null);
						allMaterials.add(m);
					}
					WorkOrder w = new WorkOrder(null, workDate, email, email, debtorCode, offerteStatus + "",
							paymentMethod, allMaterials, workDate, null, id, null, allRelations, null, null, null,
							offerteNr, typeOfWork, description, modified);
					offertes.add(w);
				}
			}
		}
		if (!offertes.isEmpty()) {
			// ObjectDAO.saveOffertes(offertes, softwareToken);
			JSONArray responseArray = (JSONArray) WorkOrderHandler.addData(softwareToken, offertes, "PostWorkorders",
					softwareName, clientToken);
			for (int i = 0; i < responseArray.length(); i++) {
				JSONObject object = responseArray.getJSONObject(i);
				int id = object.getInt("workorder_no");
				WeFactHandler wf = new WeFactHandler();
				wf.setOfferteStatus(clientToken, id, true);
			}
			int successAmount = responseArray.length();
			if (successAmount > 0) {
				checkUpdate = true;
				errorMessage += "Success " + successAmount + " offertes imported<br />";
			} else {
				errorMessage += "Something went wrong with offertes<br />";
			}
		} else {
			errorMessage += "No offerte for import<br />";
		}
		return new String[] { errorMessage, checkUpdate + "" };
	}

	public String[] setFactuur(String clientToken, String token, String factuurType) throws IOException, JSONException {
		String errorMessage = "", errorDetails = "";
		String jsonRequest = null;
		int exportAmount = 0, successAmount = 0, errorAmount = 0;
		controller = "invoice";
		action = "add";
		// Get WorkOrders
		ArrayList<WorkOrder> allData = WorkOrderHandler.getData(token, "GetWorkorders", factuurType, false,
				softwareName);
		for (WorkOrder w : allData) {
			exportAmount++;
			for (Relation r : w.getRelations()) {
				Address a = r.getAddressess().get(0);
				if (a.getType().equals("invoice")) {

					// Map date
					String workDate = null;
					try {
						SimpleDateFormat dt = new SimpleDateFormat("dd-MM-yyyy");
						Date formatDate = dt.parse(w.getWorkDate());
						SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
						workDate = dt1.format(formatDate);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					jsonRequest = "{\"api_key\":\"" + clientToken + "\"," + "\"controller\":\"" + controller + "\","
							+ "\"action\":\"" + action + "\"," + "\"DebtorCode\":\"" + w.getCustomerDebtorNr() + "\","
							+ "\"Date\":\"" + workDate + "\"," + "\"ReferenceNumber\":\"" + w.getExternProjectNr()
							+ "\"," + "\"CompanyName\":\"" + r.getCompanyName() + "\"," + "\"Initials\":\""
							+ a.getName() + "\"," + "\"Address\":\"" + a.getStreet() + "\"," + "\"ZipCode\":\""
							+ a.getPostalCode() + "\"," + "\"City\":\"" + a.getCity() + "\"," + "\"EmailAddress\":\""
							+ a.getEmail() + "\"," + "\"Description\":\""
							+ w.getWorkDescription().replaceAll("[\\t\\n\\r]+", " ") + "\","
							+ "\"CustomFields[typeofwork]\":\"" + w.getTypeOfWork() + "\","
							+ "\"CustomFields[paymentmethod]\":\"" + w.getPaymentMethod() + "\","
							+ "\"InvoiceLines\":[";
					int i = 0;
					for (Material m : w.getMaterials()) {
						i++;
						if (i == w.getMaterials().size()) {
							jsonRequest += "{\"ProductCode\":\"" + m.getCode() + "\"," + "\"Number\":\""
									+ m.getQuantity() + "\"}";
						} else {
							jsonRequest += "{\"ProductCode\":\"" + m.getCode() + "\"," + "\"Number\":\""
									+ m.getQuantity() + "\"},";
						}
					}
					i = 0;
					for (WorkPeriod p : w.getWorkPeriods()) {
						i++;
						double number = p.getDuration();
						double quantity = (number / 60);
						if (w.getMaterials().size() != 0) {
							if (i == w.getWorkPeriods().size()) {
								jsonRequest += "{\"ProductCode\":\"" + p.getHourType() + "\"," + "\"Number\":\""
										+ quantity + "\"}";
							} else {
								jsonRequest += ",{\"ProductCode\":\"" + p.getHourType() + "\"," + "\"Number\":\""
										+ quantity + "\"},";
							}
						} else {
							if (i == w.getWorkPeriods().size()) {
								jsonRequest += "{\"ProductCode\":\"" + p.getHourType() + "\"," + "\"Number\":\""
										+ quantity + "\"}";
							} else {
								jsonRequest += "{\"ProductCode\":\"" + p.getHourType() + "\"," + "\"Number\":\""
										+ quantity + "\"},";
							}
						}
					}
					jsonRequest += "]}";
				}

			}
			JSONObject jsonList = getJsonResponse(clientToken, controller, action, array, jsonRequest);
			String status = jsonList.getString("status");
			if (status.equals("success")) {
				successAmount++;
				// Set status to afgehandeld
				WorkOrderHandler.setWorkorderStatus(w.getId(), w.getWorkorderNr(), true, "GetWorkorder", token,
						softwareName);
			} else {
				errorAmount++;
				errorDetails = jsonList + "";
			}
			System.out.println("JSON_REQUEST_FACTUUR " + jsonRequest);
		}
		if (errorAmount > 0) {
			errorMessage += errorAmount + " out of " + exportAmount + " workorders(factuur) have errors<br>";
		}
		if (successAmount > 0) {
			errorMessage += successAmount + " workorders(factuur) exported<br>";
		}
		return new String[] { errorMessage, errorDetails };
	}

	public String[] setOfferte(String clientToken, String token, String factuurType) throws IOException, JSONException {
		String errorMessage = "", errorDetails = "";
		String jsonRequest = null;
		int exportAmount = 0, successAmount = 0, errorAmount = 0;
		controller = "pricequote";
		action = "add";
		// Get WorkOrders
		ArrayList<WorkOrder> allData = WorkOrderHandler.getData(token, "GetWorkorders", factuurType, false,
				softwareName);
		for (WorkOrder w : allData) {
			exportAmount++;
			for (Relation r : w.getRelations()) {
				Address a = r.getAddressess().get(0);
				if (a.getType().equals("invoice")) {

					// Map date
					String workDate = null;
					try {
						SimpleDateFormat dt = new SimpleDateFormat("dd-MM-yyyy");
						Date formatDate = dt.parse(w.getWorkDate());
						SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
						workDate = dt1.format(formatDate);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					jsonRequest = "{\"api_key\":\"" + clientToken + "\"," + "\"controller\":\"" + controller + "\","
							+ "\"action\":\"" + action + "\"," + "\"PriceQuoteCode\":\"" + w.getExternProjectNr()
							+ "\"," + "\"DebtorCode\":\"" + w.getCustomerDebtorNr() + "\"," + "\"Date\":\"" + workDate
							+ "\"," + "\"CompanyName\":\"" + r.getCompanyName() + "\"," + "\"Initials\":\""
							+ a.getName() + "\"," + "\"Address\":\"" + a.getStreet() + "\"," + "\"ZipCode\":\""
							+ a.getPostalCode() + "\"," + "\"City\":\"" + a.getCity() + "\"," + "\"EmailAddress\":\""
							+ a.getEmail() + "\"," + "\"Description\":\""
							+ w.getWorkDescription().replaceAll("[\\t\\n\\r]+", " ") + "\","
							+ "\"CustomFields[typeofwork]\":\"" + w.getTypeOfWork() + "\","
							+ "\"CustomFields[paymentmethod]\":\"" + w.getPaymentMethod() + "\","
							+ "\"InvoiceLines\":[";
					int i = 0;
					for (Material m : w.getMaterials()) {
						i++;
						if (i == w.getMaterials().size()) {
							jsonRequest += "{\"ProductCode\":\"" + m.getCode() + "\"," + "\"Number\":\""
									+ m.getQuantity() + "\"}";
						} else {
							jsonRequest += "{\"ProductCode\":\"" + m.getCode() + "\"," + "\"Number\":\""
									+ m.getQuantity() + "\"},";
						}
					}
					i = 0;
					for (WorkPeriod p : w.getWorkPeriods()) {
						i++;
						double number = p.getDuration();
						System.out.println("NUMMER " + number);
						double quantity = (number / 60);
						System.out.println("NUMMER " + quantity);
						if (w.getMaterials().size() != 0) {
							if (i == w.getWorkPeriods().size()) {
								jsonRequest += ",{\"ProductCode\":\"" + p.getHourType() + "\"," + "\"Number\":\""
										+ quantity + "\"}";
							} else {
								jsonRequest += ",{\"ProductCode\":\"" + p.getHourType() + "\"," + "\"Number\":\""
										+ quantity + "\"},";
							}
						} else {
							if (i == w.getWorkPeriods().size()) {
								jsonRequest += "{\"ProductCode\":\"" + p.getHourType() + "\"," + "\"Number\":\""
										+ quantity + "\"}";
							} else {
								jsonRequest += "{\"ProductCode\":\"" + p.getHourType() + "\"," + "\"Number\":\""
										+ quantity + "\"},";
							}
						}
					}
					jsonRequest += "]}";
				}

			}
			JSONObject jsonList = getJsonResponse(clientToken, controller, action, array, jsonRequest);
			String status = jsonList.getString("status");
			if (status.equals("success")) {
				successAmount++;
				//Set status to afgehandeld
				WorkOrderHandler.setWorkorderStatus(w.getId(), w.getWorkorderNr(), true, "GetWorkorder", token,
						softwareName);
			} else {
				errorAmount++;
				errorDetails += jsonList + "";
				System.out.println("JSON_REQUEST_OFFERTE " + jsonRequest);
			}
		}
		if (errorAmount > 0) {
			errorMessage += errorAmount + " out of " + exportAmount + " workorders(offerte) have errors<br>";
		}
		if (successAmount > 0) {
			errorMessage += successAmount + " workorders(offerte) exported";
		}
		return new String[] { errorMessage, errorDetails };
	}

	// Set offerte status after sending to WerkbonApp
	public void setOfferteStatus(String clientToken, int id, Boolean accepted) throws IOException, JSONException {
		controller = "pricequote";
		action = "edit";
		array = "&Identifier=" + id;
		if (accepted) {
			array += "&CustomFields[werkbonappadded]=1";
		} else {
			array += "&CustomFields[werkbonappadded]=0";
		}
		JSONObject jsonList = getJsonResponse(clientToken, controller, action, array, null);
	}

}
