package controller.accountview;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.*;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import DAO.ObjectDAO;
import controller.WorkOrderHandler;
import controller.twinfield.SoapHandler;
import object.workorder.Address;
import object.workorder.HourType;
import object.workorder.Material;
import object.workorder.Relation;
import object.workorder.WorkOrder;
import object.workorder.WorkPeriod;

public class AccountViewHandler {
	private String controller, action;
	private String array = null;
	final String softwareName = "WeFact";
	private Boolean checkUpdate = false;
	private final static Logger logger = Logger.getLogger(SoapHandler.class.getName());

	public HttpURLConnection getConnection(int postDataLength, String jsonRequest) throws IOException {
		URL proxyUrl = null;
		if (System.getenv("QUOTAGUARDSTATIC_URL") != null) {
			proxyUrl = new URL(System.getenv("QUOTAGUARDSTATIC_URL"));
		} else {
			proxyUrl = new URL("http://quotaguard9873:dd601d82a5cc@us-east-1-static.quotaguard.com:9293");
		}
		String userInfo = proxyUrl.getUserInfo();
		String user = userInfo.substring(0, userInfo.indexOf(':'));
		String password = userInfo.substring(userInfo.indexOf(':') + 1);

		HttpURLConnection conn = null;
		System.setProperty("http.proxyHost", proxyUrl.getHost());
		System.setProperty("http.proxyPort", Integer.toString(proxyUrl.getPort()));

		Authenticator.setDefault(new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(user, password.toCharArray());
			}
		});
		URL url = new URL("http://ip.quotaguard.com");
		conn = (HttpURLConnection) url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

		String inputLine;
		while ((inputLine = in.readLine()) != null)
			System.out.println(inputLine);

		in.close();

		String contentType = null;
		url = new URL("https://www.mijnwefact.nl/apiv2/api.php");
		if (jsonRequest == null) {
			contentType = "application/x-www-form-urlencoded";
		} else {
			contentType = "application/json";
		}
		conn = (HttpURLConnection) url.openConnection();
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

	public Object checkClientToken(String clientToken) throws IOException {
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
			} else {
				String errorMessage = null;
				JSONArray array = json.getJSONArray("errors");
				for (int i = 0; i < array.length(); i++) {
					Object obj = array.get(i);
					errorMessage = obj + "<br>";
				}
				return errorMessage;
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
			cal.add(Calendar.HOUR_OF_DAY, 0);
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
		logger.info("Material response " + jsonList);
		String status = jsonList.getString("status");
		if (status.equals("success")) {
			int totalResults = jsonList.getInt("totalresults");
			if (totalResults > 0) {
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
					// if(description.equals("")){
					// if(!object.getString("ProductKeyPhrase").equals("")){
					// description = object.getString("ProductKeyPhrase");
					// }
					// }
					Double price = object.getDouble("PriceExcl");
					String unit = object.getString("NumberSuffix");
					Material m = new Material(productCode, null, unit, description, price, null, modified);
					materials.add(m);
				}
			}
		} else {
			JSONArray array = jsonList.getJSONArray("errors");
			for (int i = 0; i < array.length(); i++) {
				Object obj = array.get(i);
				errorMessage += obj + "<br>";
			}
		}
		if (!materials.isEmpty()) {
			int successAmount = (int) WorkOrderHandler.addData(softwareToken, materials, "materials", softwareName,
					null);
			if (successAmount > 0) {
				ObjectDAO.saveMaterials(materials, softwareToken);
				errorMessage += importCount + " materials imported<br>";
				errorMessage += "and " + editCount + " materials edited<br>";
				checkUpdate = true;
			} else {
				errorMessage += "Something went wrong with Materials<br>";
			}
		} else {
			errorMessage += "No materials for import<br>";
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
		String status = jsonList.getString("status");
		// Check if ListRequest is successful
		if (status.equals("success")) {
			int totalResults = jsonList.getInt("totalresults");
			if (totalResults > 0) {
				JSONArray debtors = jsonList.getJSONArray("debtors");
				for (int i = 0; i < debtors.length(); i++) {
					JSONObject object = debtors.getJSONObject(i);
					String debtorCode = object.getString("DebtorCode");
					action = "show";
					String debtorArray = "&DebtorCode=" + debtorCode;
					JSONObject jsonShow = getJsonResponse(clientToken, controller, action, debtorArray, null);
					logger.info("relation response " + jsonShow);
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
						// if (email.equals("")) {
						// email = "<leeg>";
						// }
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
							street = "<leeg>";
						}
						String postalCode = debtorDetails.getString("ZipCode");
						if (postalCode.equals("")) {
							postalCode = "<leeg>";
						}
						String city = debtorDetails.getString("City");
						if (city.equals("")) {
							city = "<leeg>";
						}
						String remark = debtorDetails.getString("Comment");

						Address postal = new Address(contact, phoneNr, email, street, houseNumber, postalCode, city,
								remark, "postal", 2);
						address.add(postal);
						// Invoice
						String invoiceFirstName = debtorDetails.getString("InvoiceInitials");
						String invoiceLastName = debtorDetails.getString("InvoiceSurName");
						String invoiceContact = invoiceFirstName + " " + invoiceLastName;
						String invoiceCompanyName = debtorDetails.getString("InvoiceCompanyName");
						if (!invoiceCompanyName.equals("")) {
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
							invoicestreet = "<leeg>";
						}
						String invoicepostalCode = debtorDetails.getString("InvoiceZipCode");
						if (invoicepostalCode.equals("")) {
							invoicepostalCode = "<leeg>";
						}
						String invoicecity = debtorDetails.getString("InvoiceCity");
						if (invoicecity.equals("")) {
							invoicecity = "<leeg>";
						}
						if (!invoicecity.equals("<leeg>")) {
							Address invoice = new Address(invoiceContact, phoneNr, invoiceEmail, invoicestreet,
									invoicehouseNumber, invoicepostalCode, invoicecity, remark, "invoice", 1);
							address.add(invoice);
						}

						r = new Relation(companyName, debtorNr, contact, invoiceEmail, address, modified);
						relations.add(r);
					}
				}
			}
		} else {
			JSONArray array = jsonList.getJSONArray("errors");
			for (int i = 0; i < array.length(); i++) {
				Object obj = array.get(i);
				errorMessage += obj + "<br>";
			}
		}
		if (!relations.isEmpty()) {
			int successAmount = (int) WorkOrderHandler.addData(softwareToken, relations, "relations", softwareName,
					null);
			if (successAmount > 0) {
				ObjectDAO.saveRelations(relations, softwareToken);
				errorMessage += importCount + " relations imported<br>";
				errorMessage += "and " + editCount + " relations edited<br>";
				checkUpdate = true;
			} else {
				errorMessage += "Something went wrong with Relations<br>";
			}
		} else {
			errorMessage += "No relations for import<br>";
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
		String status = jsonList.getString("status");
		// Check if ListRequest is successful
		if (status.equals("success")) {
			int totalResults = jsonList.getInt("totalresults");
			if (totalResults > 0) {
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
						logger.info("hourtype response " + jsonList);
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
								// Calculate salesPrice with tax and
								// productPrice
								Double salePrice = costPrice * (tax / 100 + 1.00);
								int booking = 0;
								if (costPrice != null && !costPrice.equals("")) {
									booking = 1;
								}
								h = new HourType(productCode, productName, booking, booking, costPrice, salePrice, 1,
										modified);
								hourtypes.add(h);
							}
						}
					}
				}
			}
		} else {
			JSONArray array = jsonList.getJSONArray("errors");
			for (int i = 0; i < array.length(); i++) {
				Object obj = array.get(i);
				errorMessage += obj + "<br>";
			}
		}

		if (!hourtypes.isEmpty()) {
			// Send to WorkOrderApp
			int successAmount = (int) WorkOrderHandler.addData(softwareToken, hourtypes, "hourtypes", softwareName,
					null);
			if (successAmount > 0) {
				// Save to db
				ObjectDAO.saveHourTypes(hourtypes, softwareToken);
				errorMessage += importCount + " hourtypes imported<br>";
				errorMessage += "and " + editCount + " hourtypes edited<br>";
				checkUpdate = true;
			} else {
				errorMessage += "Something went wrong with hourtypes<br>";
			}
		} else {
			errorMessage += "No hourtypes for import<br>";
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

		// Check if ListRequest is successful
		if (status.equals("success")) {
			int totalResults = jsonList.getInt("totalresults");
			if (totalResults > 0) {
				JSONArray offerteList = jsonList.getJSONArray("pricequotes");
				for (int i = 0; i < offerteList.length(); i++) {
					JSONObject offerteObject = offerteList.getJSONObject(i);
					String offerteNr = offerteObject.getString("PriceQuoteCode");
					action = "show";
					String offerteArray = "&PriceQuoteCode=" + offerteNr;
					JSONObject jsonShow = getJsonResponse(clientToken, controller, action, offerteArray, null);
					logger.info("offerte response " + jsonShow);
					String statusShow = jsonShow.getString("status");
					// custom fields
					JSONObject offerteDetails = jsonShow.getJSONObject("pricequote");
					JSONObject customFields = offerteDetails.getJSONObject("CustomFields");
					int werkbonApp = customFields.getInt("werkbonapp");
					int werkbonAppAdded = customFields.getInt("werkbonappadded");
					String typeOfWork = customFields.getString("typeofwork");
					String paymentMethod = customFields.getString("paymentmethod");
					if (typeOfWork == null) {
						typeOfWork = "<leeg>";
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
							street = "<leeg>";
						}
						String postalCode = offerteDetails.getString("ZipCode");
						if (postalCode.equals("")) {
							postalCode = "<leeg>";
						}
						String city = offerteDetails.getString("City");
						if (city.equals("")) {
							city = "<leeg>";
						}
						String email = offerteDetails.getString("EmailAddress");
						// if (email.equals("")) {
						// email = "<leeg>";
						// }
						String description = offerteDetails.getString("Description");
						// String comment = offerteDetails.getString("Comment");
						// Invoice relation from database
						Relation dbRelation = ObjectDAO.getRelation(softwareToken, debtorCode, "invoice");
						if (dbRelation == null) {
							// Get postal address
							dbRelation = ObjectDAO.getRelation(softwareToken, debtorCode, "postal");
						}
						if (dbRelation != null) {
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
									paymentMethod, allMaterials, workDate, null, id, null, allRelations, null, null,
									null, offerteNr, typeOfWork, description, modified, null, null);
							offertes.add(w);
						}
					}
				}
			}
		} else {
			JSONArray array = jsonList.getJSONArray("errors");
			for (int i = 0; i < array.length(); i++) {
				Object obj = array.get(i);
				errorMessage += obj + "<br>";
			}
		}
		if (!offertes.isEmpty()) {
			// ObjectDAO.saveOffertes(offertes, softwareToken);
			JSONArray responseArray = (JSONArray) WorkOrderHandler.addData(softwareToken, offertes, "PostWorkorders",
					softwareName, clientToken);
			for (int i = 0; i < responseArray.length(); i++) {
				JSONObject object = responseArray.getJSONObject(i);
				int id = object.getInt("workorder_no");
				AccountViewHandler wf = new AccountViewHandler();
				wf.setOfferteStatus(clientToken, id, true);
			}
			int successAmount = responseArray.length();
			if (successAmount > 0) {
				checkUpdate = true;
				errorMessage += successAmount + " offertes imported<br>";
			} else {
				errorMessage += "Something went wrong with offertes<br>";
			}
		} else {
			errorMessage += "No offerte for import<br>";
		}
		return new String[] { errorMessage, checkUpdate + "" };
	}

	public String[] setFactuur(String clientToken, String token, String factuurType) throws IOException, JSONException {
		String errorMessage = "", errorDetails = "";
		String jsonRequest = null;
		JSONArray JSONArray = null;
		JSONObject JSONObject = null;
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
					JSONArray = new JSONArray();
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
					JSONObject = new JSONObject();
					String reference = w.getWorkorderNr() + " - " + w.getExternProjectNr();
					try {
						JSONObject.put("api_key", clientToken);
						JSONObject.put("controller", controller);
						JSONObject.put("action", action);
						JSONObject.put("InvoiceCode", w.getId());
						JSONObject.put("DebtorCode", w.getCustomerDebtorNr());
						JSONObject.put("Date", workDate);
						JSONObject.put("ReferenceNumber", reference);
						JSONObject.put("CompanyName", r.getCompanyName());
						JSONObject.put("Initials", a.getName());
						JSONObject.put("Address", a.getStreet());
						JSONObject.put("ZipCode", a.getPostalCode());
						JSONObject.put("City", a.getCity());
						JSONObject.put("EmailAddress", a.getEmail());
						JSONObject.put("Description", w.getWorkDescription());
						// CHANGE
//						JSONObject.put("Status", 0);
						
						JSONObject JSONObjectCustom = new JSONObject();
						JSONObjectCustom.put("werkbontype", w.getTypeOfWork());
						JSONObjectCustom.put("paymentmethod", w.getPaymentMethod());
						JSONObject.put("CustomFields", JSONObjectCustom);
						JSONObject JSONObjectMaterial = null;
						for (Material m : w.getMaterials()) {
							JSONObjectMaterial = new JSONObject();
							JSONObjectMaterial.put("ProductCode", m.getCode());
							JSONObjectMaterial.put("Number", m.getQuantity());
							JSONArray.put(JSONObjectMaterial);
						}
						JSONObject JSONObjectWorkPeriod = null;
						for (WorkPeriod p : w.getWorkPeriods()) {
							double number = p.getDuration();
							double urenInteger = (number % 15);
							if (urenInteger < 8) {
								number = number - urenInteger;
							} else {
								number = number - urenInteger + 15;
							}
							double quantity = (number / 60);
							JSONObjectWorkPeriod = new JSONObject();
							JSONObjectWorkPeriod.put("ProductCode", p.getHourType());
							JSONObjectWorkPeriod.put("Number", quantity);
							JSONArray.put(JSONObjectWorkPeriod);
						}
						JSONObject.put("InvoiceLines", JSONArray);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

			}
			logger.info("factuur request " + JSONObject);
			JSONObject jsonList = getJsonResponse(clientToken, controller, action, array, JSONObject + "");
			String status = jsonList.getString("status");
			if (status.equals("success")) {
				Boolean b = null;
				try {
					b = setAttachement(clientToken,w.getId(), w.getPdfUrl());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("setAttachement = " + b);
				successAmount++;
				// Set status to afgehandeld
				WorkOrderHandler.setWorkorderStatus(w.getId(), w.getWorkorderNr(), true, "GetWorkorder", token,
						softwareName);
			} else {
				errorAmount++;
				JSONArray array = jsonList.getJSONArray("errors");
				for (int i = 0; i < array.length(); i++) {
					String projectNr = "";
					if (!w.getWorkorderNr().equals("")) {
						projectNr = w.getWorkorderNr();
					} else if (!w.getProjectNr().equals("")) {
						projectNr = w.getProjectNr();
					} else if (!w.getExternProjectNr().equals("")) {
						projectNr = w.getExternProjectNr();
					} else {
						projectNr = "<leeg>";
					}
					Object obj = array.get(i);
					errorDetails += obj + " for WorkOrder " + projectNr + " with relation " + w.getCustomerDebtorNr()
							+ " \n";
				}
			}
		}
		if (errorAmount > 0) {
			errorMessage += errorAmount + " out of " + exportAmount + " workorders(factuur) have errors<br>";
		}
		if (successAmount > 0) {
			errorMessage += successAmount + " workorders(factuur) exported<br>";
		}
		return new String[] { errorMessage, errorDetails };
	}

	private String encodeFileToBase64Binary(String pdfUrl) throws IOException {
		URL url = new URL(pdfUrl);
		System.out.println("pdfURL " + pdfUrl);
		InputStream in = url.openStream();
		Files.copy(in, Paths.get("Werkbon.pdf"), StandardCopyOption.REPLACE_EXISTING);
		in.close();
		File file = new File("Werkbon.pdf");
		byte[] bytes = loadFile(file);
		byte[] encoded = Base64.encodeBase64(bytes);
		String encodedString = new String(encoded);
		System.out.println("encodedString " + encodedString);
		return encodedString;
	}

	private static byte[] loadFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		long length = file.length();
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}
		byte[] bytes = new byte[(int) length];

		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		if (offset < bytes.length) {
			throw new IOException("Could not completely read file " + file.getName());
		}

		is.close();
		return bytes;
	}
	public Boolean setAttachement(String clientToken, String id, String url) throws Exception {
		JSONObject JSONObject = new JSONObject();
		controller = "attachment";
		action = "add";
		Boolean b;
		String base64 = encodeFileToBase64Binary(url);
		try {
			JSONObject.put("api_key", clientToken);
			JSONObject.put("controller", controller);
			JSONObject.put("action", action);
			JSONObject.put("InvoiceCode", id);
			JSONObject.put("Type", "invoice");
			JSONObject.put("Filename", "Werkbon_"+id+".pdf");
			JSONObject.put("Base64", base64);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		JSONObject jsonList = getJsonResponse(clientToken, controller, action, array, JSONObject+"");
		System.out.println("--------------");
		System.out.println(base64);
		System.out.println("--------------");	
		logger.info("Attachement response " + jsonList);
		String status = jsonList.getString("status");
		if (status.equals("success")) {
			b = true;
		} else {
			b = false;
		}
		return b;
	}
	
	public String[] setOfferte(String clientToken, String token, String factuurType) throws IOException, JSONException {
		String errorMessage = "", errorDetails = "";
		String jsonRequest = null;
		JSONArray JSONArray = null;
		JSONObject JSONObject = null;
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
					JSONArray = new JSONArray();
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
					if (w.getExternProjectNr() != null && !w.getExternProjectNr().equals("")) {
						action = "edit";
					} else {
						action = "add";
					}
					if (!w.getExternProjectNr().equals("")) {
						System.out.println("w.getExternProjectNr()" + w.getExternProjectNr() + "heeey");
					}

					JSONObject = new JSONObject();
					try {
						JSONObject.put("api_key", clientToken);
						JSONObject.put("controller", controller);
						JSONObject.put("action", action);
						JSONObject.put("PriceQuoteCode", w.getExternProjectNr());
						JSONObject.put("Date", workDate);
						JSONObject.put("ReferenceNumber", "");
						JSONObject.put("CompanyName", r.getCompanyName());
						JSONObject.put("Initials", a.getName());
						JSONObject.put("Address", a.getStreet());
						JSONObject.put("ZipCode", a.getPostalCode());
						JSONObject.put("City", a.getCity());
						JSONObject.put("EmailAddress", a.getEmail());
						JSONObject.put("Description", w.getWorkDescription());

						JSONObject JSONObjectCustom = new JSONObject();
						JSONObjectCustom.put("werkbontype", w.getTypeOfWork());
						JSONObjectCustom.put("paymentmethod", w.getPaymentMethod());
						JSONObject.put("CustomFields", JSONObjectCustom);
						JSONObject JSONObjectMaterial = null;
						for (Material m : w.getMaterials()) {
							JSONObjectMaterial = new JSONObject();
							JSONObjectMaterial.put("ProductCode", m.getCode());
							JSONObjectMaterial.put("Number", m.getQuantity());
							JSONArray.put(JSONObjectMaterial);
						}
						JSONObject JSONObjectWorkPeriod = null;
						for (WorkPeriod p : w.getWorkPeriods()) {
							double number = p.getDuration();
							double urenInteger = (number % 15);
							if (urenInteger < 8) {
								number = number - urenInteger;
							} else {
								number = number - urenInteger + 15;
							}
							double quantity = (number / 60);
							JSONObjectWorkPeriod = new JSONObject();
							JSONObjectWorkPeriod.put("ProductCode", p.getHourType());
							JSONObjectWorkPeriod.put("Number", quantity);
							JSONArray.put(JSONObjectWorkPeriod);
						}
						JSONObject.put("PriceQuoteLines", JSONArray);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

			}
			logger.info("offerte request " + JSONObject);
			JSONObject jsonList = getJsonResponse(clientToken, controller, action, array, JSONObject + "");
			String status = jsonList.getString("status");
			if (status.equals("success")) {
				successAmount++;
				// Set status to afgehandeld
				WorkOrderHandler.setWorkorderStatus(w.getId(), w.getWorkorderNr(), true, "GetWorkorder", token,
						softwareName);
			} else {
				errorAmount++;
				JSONArray array = jsonList.getJSONArray("errors");
				for (int i = 0; i < array.length(); i++) {
					String projectNr = "";
					if (!w.getWorkorderNr().equals("")) {
						projectNr = w.getWorkorderNr();
					} else if (!w.getProjectNr().equals("")) {
						projectNr = w.getProjectNr();
					} else if (!w.getExternProjectNr().equals("")) {
						projectNr = w.getExternProjectNr();
					} else {
						projectNr = "<leeg>";
					}
					Object obj = array.get(i);
					errorDetails += obj + " for WorkOrder " + projectNr + " with relation " + w.getCustomerDebtorNr()
							+ " \n";
				}
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